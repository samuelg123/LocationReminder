package com.udacity.project4.base

import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.common.api.ApiException
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

    protected val navHostFragment by lazy { supportFragmentManager.findFragmentById(navHostId) as NavHostFragment }
    protected val navController get() = navHostFragment.navController

    private val resolutionForResult = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                resolutionRequestCompletable.complete(true)
                //startLocationUpdates() or do whatever you want
            } else {
                resolutionRequestCompletable.complete(false)
            }
        }

    private val permissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            for ((_, isGranted) in result) {
                if (!isGranted) {
                    permissionRequestCompletable.complete(false)
                    return@registerForActivityResult
                }
            }
            permissionRequestCompletable.complete(true)
        }

    private lateinit var permissionRequestCompletable: CompletableDeferred<Boolean>
    private lateinit var resolutionRequestCompletable: CompletableDeferred<Boolean>
    private val permissionMutex = Mutex()
    private val resolutionMutex = Mutex()

    suspend fun requestForPermissions(vararg permissions: String): Boolean = permissionMutex.withLock {
        permissionRequestCompletable = CompletableDeferred()
        permissionResultLauncher.launch(arrayOf(*permissions))
        return permissionRequestCompletable.await()
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
            }
        } ?: false
    }

    fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved =
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

    suspend fun requestForegroundAndBackgroundLocationPermissions(): Boolean {
        if (foregroundAndBackgroundLocationPermissionApproved()) return true
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
        }
        return requestForPermissions(*permissionsArray)
    }

    suspend fun enableLocationService(): Boolean {
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval((10 * 1000).toLong())
            .setFastestInterval((1 * 1000).toLong())

        val settingsBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
        var states: LocationSettingsStates? = null

        suspend fun locationSettings() =
            LocationServices.getSettingsClient(this)
                .checkLocationSettings(settingsBuilder.build()).asDeferred().await()

        try {
            states = locationSettings().locationSettingsStates
        } catch (ex: ApiException) {
            when (ex.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    val isGranted = ex.status.resolutionForResult()
                    if (isGranted) states = locationSettings().locationSettingsStates
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
        return states?.isGpsUsable == true
    }

}