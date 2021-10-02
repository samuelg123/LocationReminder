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
import androidx.annotation.VisibleForTesting
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
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.EspressoIdlingResource
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.toast
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*


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

    val onSelectedCompleter = CompletableDeferred<Boolean>()

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

        viewModel.loadReminderDataItem()

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return binding.root
    }

    override fun onDestroy() {
        viewModel.clearCurrentLocation()
        super.onDestroy()
    }

    fun onLocationSelected() {
        viewModel.commitLocation()
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

        googleMap.setOnMapClickListener(::onClickMap)

        googleMap.setOnPoiClickListener(::onClickPoi)

        lifecycleScope.launchWhenStarted {
            enableMyLocation()
        }
        viewModel.latLngCheck.observe(viewLifecycleOwner) {
            onSelectedCompleter.complete(it)
        }
        viewModel.tempDataItem.observe(viewLifecycleOwner) {
            it?.run {
                latitude?.let { lat ->
                    longitude?.let { lng ->
                        onUpdateLocation(
                            it.location ?: getString(R.string.unknown_location),
                            LatLng(lat, lng)
                        )
                    }
                }
            }
        }
        isMapReady.complete(true)
    }

    fun onClickMap(latLng: LatLng) {
        viewModel.tempDataItem.value = viewModel.tempDataItem.value?.copy(
            latitude = latLng.latitude,
            longitude = latLng.longitude,
            location = getString(R.string.custom_location),
        ) ?: ReminderDataItem(
            latitude = latLng.latitude,
            longitude = latLng.longitude,
            location = getString(R.string.custom_location),
        )
    }

    fun onClickPoi(poi: PointOfInterest) {
        viewModel.tempDataItem.value = viewModel.tempDataItem.value?.copy(
            latitude = poi.latLng.latitude,
            longitude = poi.latLng.longitude,
            location = poi.name
        ) ?: ReminderDataItem(
            latitude = poi.latLng.latitude,
            longitude = poi.latLng.longitude,
            location = poi.name
        )
    }

    private fun onUpdateLocation(name: String, latLng: LatLng) {
        gotoLocation(latLng)
        setMarker(latLng, name)?.showInfoWindow()
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
        val lastLocation = getLastLocation() ?: return
        val homeLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
        withContext(Dispatchers.Main) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, 15F))
        }
    }

    private fun gotoLocation(latLng: LatLng) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLastLocation(): Location? {
        if (isPermissionGranted() && isLocationEnabled()) {
            return fusedLocationClient.getCurrentLocation(
                PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).asDeferred().await()
        }
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
    private suspend fun enableMyLocation() = withContext(Dispatchers.Main) {
        viewModel.permissionGranted = isPermissionGranted()
        if (!viewModel.permissionGranted) {
            viewModel.permissionGranted = requestLocationPermission()
        }
        if (viewModel.permissionGranted) {
            viewModel.locationEnabled = parentActivity.enableLocationService()
            if (!viewModel.locationEnabled) {
                toast(R.string.location_required_error)
                return@withContext
            }
        } else {
            toast(R.string.permission_denied_explanation)
            return@withContext
        }
        viewModel.tempDataItem.value?.run {
            if (latitude == null || longitude == null) gotoMyLocation()
        }
        googleMap.isMyLocationEnabled = true
    }

    private suspend fun requestLocationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }
            parentActivity.requestForPermissions(*permissions)
        } else true

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
