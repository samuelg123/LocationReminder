package com.udacity.project4.base

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.asDeferred

abstract class BaseActivity : AppCompatActivity() {

    @get:IdRes
    abstract val navHostId: Int

    val navHostFragment by lazy { supportFragmentManager.findFragmentById(navHostId) as NavHostFragment }
    protected val navController get() = navHostFragment.navController

    private val resolutionForResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                resolutionRequestCompletable.complete(true)
                //startLocationUpdates() or do whatever you want
            } else {
                resolutionRequestCompletable.complete(false)
            }
        }

    private val permissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            permissionRequestCompletable.complete(result)
        }

    private lateinit var permissionRequestCompletable: CompletableDeferred<MutableMap<String, Boolean>>
    private lateinit var resolutionRequestCompletable: CompletableDeferred<Boolean>
    private val permissionMutex = Mutex()
    private val resolutionMutex = Mutex()

    suspend fun requestForPermissions(vararg permission: String): Boolean =
        permissionMutex.withLock {
            permissionRequestCompletable = CompletableDeferred()
            permissionResultLauncher.launch(permission)
            for ((_, isGranted) in permissionRequestCompletable.await().entries.asIterable()) {
                if (!isGranted) return@withLock false
            }
            return@withLock true
        }

    private suspend fun Status.resolutionForResult(): Boolean = resolutionMutex.withLock {
        return@withLock this.resolution?.let {
            resolutionRequestCompletable = CompletableDeferred()
            return try {
                val intentSenderRequest = IntentSenderRequest.Builder(it).build()
                resolutionForResult.launch(intentSenderRequest)
                resolutionRequestCompletable.await()
            } catch (e: IntentSender.SendIntentException) {
                // Ignore the error.
                false
            } catch (e: ClassCastException) {
                // Ignore, should be an impossible error.
                false
            } catch (e: Throwable) {
                false
            }
        } ?: false
    }

    fun isForegroundLocationPermissionGranted(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    fun isForegroundAndBackgroundLocationPermissionGranted(): Boolean {
        val foregroundLocationApproved = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val backgroundPermissionApproved =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
            else true

        return foregroundLocationApproved && backgroundPermissionApproved
    }

    suspend fun requestLocationPermission(useBackgroundLocation: Boolean): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && useBackgroundLocation) {
                permissions += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            }
            requestForPermissions(*permissions)
        } else true

    suspend fun enableLocationServiceSettings(priority: Int = LocationRequest.PRIORITY_LOW_POWER): Boolean {
        val locationRequest = LocationRequest.create()
            .setPriority(priority)
            .setInterval((10 * 1000).toLong())
            .setFastestInterval((1 * 1000).toLong())

        val settingsBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
        var isGranted = false

        try {
            LocationServices.getSettingsClient(this)
                .checkLocationSettings(settingsBuilder.build())
                .asDeferred().await()
            isGranted = true
        } catch (ex: ResolvableApiException) {
            when (ex.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    //this should enable device gps
                    isGranted = ex.status.resolutionForResult()
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
        }
        return isGranted
    }

    fun isDeviceLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }
}