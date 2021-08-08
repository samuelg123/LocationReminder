package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.annotation.RawRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.asDeferred
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var googleMap: GoogleMap
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
        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return binding.root
    }

    fun onLocationSelected() {
        findNavController().navigateUp()
    }

    private fun setMarker(latLng: LatLng, title: String?): Marker? {
        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)) // change marker color to blue
        )
        _viewModel.setMarker(marker)
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

    @DelicateCoroutinesApi
    override fun onMapReady(map: GoogleMap) {
        this.googleMap = map
        setMapStyle(R.raw.mapstyles01)
        _viewModel.selectedPOI.observe(viewLifecycleOwner) {
            it?.let {
                setMarker(it.latLng, it.name)?.showInfoWindow()
            }
        }
        googleMap.setOnPoiClickListener { poi ->
            _viewModel.selectedPOI.value = poi
        }
        lifecycleScope.launch {
            val isGranted = enableGps()
            if (!isGranted) return@launch
        }
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

    private suspend fun gotoMyLoc() {
        getLastLocation()?.run {
            val homeLatLng = LatLng(latitude, longitude)
            withContext(Dispatchers.Main) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, 15F))
            }
        }
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

    private suspend fun enableGps(): Boolean {
        val locationRequest = LocationRequest.create()
            .setPriority(PRIORITY_HIGH_ACCURACY)
            .setInterval((10 * 1000).toLong())
            .setFastestInterval((1 * 1000).toLong())

        val settingsBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
        var isEnabled = true
        var states: LocationSettingsStates? = null

        suspend fun locationSettings() =
            LocationServices.getSettingsClient(requireActivity())
                .checkLocationSettings(settingsBuilder.build()).asDeferred().await()

        try {
            states = locationSettings().locationSettingsStates
        } catch (ex: ApiException) {
            when (ex.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    isEnabled = ex.status.reqGps()
                    if (isEnabled) states =
                        locationSettings().locationSettingsStates
                }
                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    /* no-op */
                }
                LocationSettingsStatusCodes.SUCCESS -> {
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                }
            }
        } catch (e: ResolvableApiException) {
            /* no-op */
        } finally {
            if (states?.isGpsUsable == true) enableMyLocation()
        }
        return isEnabled
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
        gotoMyLoc()
        withContext(Dispatchers.Main) {
            googleMap.isMyLocationEnabled = true
        }
    }

    private suspend fun requestLocationPermission(): Boolean =
        reqPermissions(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun isPermissionGranted(): Boolean = ActivityCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}
