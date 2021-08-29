package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RawRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SelectLocationFragment : BaseFragment<SaveReminderViewModel>(), OnMapReadyCallback {

    override val viewModel by sharedViewModel<SaveReminderViewModel>()

    //Use Koin to get the view model of the SaveReminder
    lateinit var googleMap: GoogleMap
    var isMapReady: CompletableDeferred<Boolean> = CompletableDeferred()
    lateinit var mapFragment: SupportMapFragment
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var binding: FragmentSelectLocationBinding

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }
    private var cancellationTokenSource = CancellationTokenSource()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)
        binding.fragment = this
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return binding.root
    }

    override fun onDestroy() {
        viewModel.clearCurrentPoi()
        super.onDestroy()
    }

    fun onLocationSelected() {
        viewModel.commitPoi()
        findNavController().navigateUp()
    }

    private fun setMarker(latLng: LatLng, title: String?): Marker? {
        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)) // change marker color to blue
        )
        viewModel.setMarker(marker)
        return marker
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        setMapStyle(R.raw.mapstyles01)
        viewModel.currentPOI.observe(viewLifecycleOwner) {
            it?.run { onUpdatePoi(this) }
        }
        googleMap.setOnPoiClickListener(::onClickPoi)
        lifecycleScope.launchWhenStarted {
            val isGranted = parentActivity.enableLocationService()
            if (isGranted) enableMyLocation()
        }
        isMapReady.complete(true)
    }

    fun onClickPoi(poi: PointOfInterest) {
        viewModel.currentPOI.value = poi
    }

    private fun onUpdatePoi(poi: PointOfInterest) {
        gotoPoi(poi)
        setMarker(poi.latLng, poi.name)?.showInfoWindow()
    }

    private fun setMapStyle(@RawRes resId: Int) {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success: Boolean = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), resId
                )
            )
            if (!success) {
                /* no-op */
            }
        } catch (e: Resources.NotFoundException) {
            /* no-op */
        }
    }

    private suspend fun gotoMyLocation() {
        getLastLocation()?.run {
            val homeLatLng = LatLng(latitude, longitude)
            withContext(Dispatchers.Main) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, 15F))
            }
        }
    }

    private fun gotoPoi(poi: PointOfInterest) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(poi.latLng, 15F))
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLastLocation(): Location? {
        if (!isPermissionGranted()) requestLocationPermission()
        if (isPermissionGranted() && isLocationEnabled()) {
            return fusedLocationClient.getCurrentLocation(
                PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            )  // or .lastLocation
                .asDeferred()
                .await()
        }
//            else {
//                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                startActivity(intent)
//            }
        return null
    }

    override fun onStop() {
        super.onStop()
        cancellationTokenSource.cancel()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager? =
            getSystemService(requireContext(), LocationManager::class.java)
        return locationManager != null && (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    @SuppressLint("MissingPermission")
    private suspend fun enableMyLocation() {
        var isGranted = isPermissionGranted()
        if (!isGranted) {
            isGranted = requestLocationPermission()
            if (!isGranted) return
        }
        if (viewModel.currentPOI.value == null) {
            gotoMyLocation()
        }
        withContext(Dispatchers.Main) {
            googleMap.isMyLocationEnabled = true
        }
    }

    private suspend fun requestLocationPermission(): Boolean =
        parentActivity.requestForPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun isPermissionGranted(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED


    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }
}
