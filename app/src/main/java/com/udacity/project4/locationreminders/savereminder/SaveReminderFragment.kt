package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.showEnableGPSDialog
import com.udacity.project4.utils.toast
import kotlinx.coroutines.tasks.asDeferred
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SaveReminderFragment : BaseFragment<SaveReminderViewModel>() {
    //Get the view model this time as a single to be shared with the another fragment
    override val viewModel by sharedViewModel<SaveReminderViewModel>()

    private lateinit var binding: FragmentSaveReminderBinding

    private lateinit var geofencingClient: GeofencingClient

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java).apply {
            action = ACTION_GEOFENCE_EVENT
        }
        PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_save_reminder,
                container,
                false
            )

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val reminderData = viewModel.reminderDataItem.value ?: return@setOnClickListener
            lifecycleScope.launchWhenStarted {
                //check both foreground and background location permission
                var isPermissionGranted =
                    parentActivity.isForegroundAndBackgroundLocationPermissionGranted()
                //request foreground permission when foreground and background location permission not granted
                if (!isPermissionGranted) isPermissionGranted =
                    parentActivity.requestLocationPermission(true)
                // still not granted? show toast
                if (!isPermissionGranted) {
                    viewModel.showToast.value = getString(R.string.permission_denied_explanation)
                    return@launchWhenStarted
                }
                // request location service settings enabled & make sure again gps/device location is enabled
                if (!parentActivity.enableLocationServiceSettings() || !parentActivity.isDeviceLocationEnabled()) {
                    toast(R.string.location_required_error)
                    showEnableGPSDialog()
                    return@launchWhenStarted
                }
                //validate data and save reminder
                if (viewModel.validateAndSaveReminder(reminderData)) {
                    //all good. adding geofence.
                    addGeofence(reminderData)
                }
            }
        }
    }

    private fun initGeofencing() {
        geofencingClient = LocationServices.getGeofencingClient(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initGeofencing()
    }


    override fun onDestroy() {
        viewModel.onClear()
        super.onDestroy()
    }

    @SuppressLint("MissingPermission")
    private suspend fun addGeofence(reminderDataItem: ReminderDataItem): Boolean {
        return reminderDataItem.latitude?.let { lat ->
            return@let reminderDataItem.longitude?.let { lng ->
                val geofence = Geofence.Builder()
                    .setRequestId(reminderDataItem.id)
                    .setCircularRegion(
                        lat,
                        lng,
                        GEOFENCE_RADIUS_IN_METERS
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()

                val geofencingRequest = GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence)
                    .build()

                return try {
                    geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                        .asDeferred()
                        .await()
                    true
                } catch (e: Exception) {
                    false
                }
            } ?: false
        } ?: false
    }

    private suspend fun removeGeofences(): Boolean {
        return try {
            geofencingClient.removeGeofences(geofencePendingIntent).asDeferred().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "RemindersActivity.locationreminders.action.ACTION_GEOFENCE_EVENT"
        const val GEOFENCE_RADIUS_IN_METERS = 300f
    }
}
