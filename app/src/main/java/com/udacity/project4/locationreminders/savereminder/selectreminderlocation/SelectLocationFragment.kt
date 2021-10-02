package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.annotation.SuppressLint
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.view.*
import androidx.annotation.RawRes
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationRequest
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
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.showEnableGPSDialog
import com.udacity.project4.utils.toast
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*


class SelectLocationFragment : BaseFragment<SaveReminderViewModel>(), OnMapReadyCallback {

    override val viewModel by sharedViewModel<SaveReminderViewModel>()

    //Use Koin to get the view model of the SaveReminder
    private lateinit var googleMap: GoogleMap
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

        viewModel.latLngCheck.observe(viewLifecycleOwner) {
            onSelectedCompleter.complete(it)
        }
        viewModel.tempSelectedDataItem.observe(viewLifecycleOwner) {
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

        lifecycleScope.launch {
            enableMyLocation()
        }
    }

    private fun onClickMap(latLng: LatLng) {
        viewModel.tempSelectedDataItem.value = viewModel.tempSelectedDataItem.value?.copy(
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
        viewModel.tempSelectedDataItem.value = viewModel.tempSelectedDataItem.value?.copy(
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
        animateMapCameraTo(latLng)
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
            animateMapCameraTo(homeLatLng)
        }
    }

    private fun animateMapCameraTo(latLng: LatLng) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLastLocation(): Location? =
        fusedLocationClient.getCurrentLocation(
            PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).asDeferred().await()

    override fun onStop() {
        super.onStop()
        cancellationTokenSource.cancel()
    }

    @SuppressLint("MissingPermission")
    private suspend fun enableMyLocation() = withContext(Dispatchers.Main) {
        //check foreground location permission
        var isPermissionGranted = parentActivity.isForegroundLocationPermissionGranted()
        //request foreground permission when foreground location permission not granted
        if (!isPermissionGranted) isPermissionGranted =
            parentActivity.requestLocationPermission(false)
        // still not granted? show toast
        if (!isPermissionGranted) {
            viewModel.showToast.value = getString(R.string.permission_denied_explanation)
            return@withContext
        }
        // request location service settings enabled & make sure again gps/device location is enabled
        if (!parentActivity.enableLocationServiceSettings(PRIORITY_HIGH_ACCURACY) || !parentActivity.isDeviceLocationEnabled()) {
            toast(R.string.location_required_error)
            showEnableGPSDialog()
            return@withContext
        }
        //enable my location button
        googleMap.isMyLocationEnabled = true
        //Go to my current location if there's no selected location
        viewModel.tempSelectedDataItem.value?.run {
            if (latitude == null || longitude == null) gotoMyLocation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }
}
