package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

class SaveReminderViewModel(
    app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {
    val reminderTitle = MutableLiveData<String>()
    val reminderDescription = MutableLiveData<String>()
    val reminderSelectedLocationStr = MutableLiveData<String>()
    val latitude = MutableLiveData<Double?>()
    val longitude = MutableLiveData<Double?>()
    val currentPOI = MutableLiveData<PointOfInterest?>()
    private var currentMarker: Marker? = null
    private var selectedPOI: PointOfInterest? = null
    var isPoiCommitted: Boolean = false

    fun setMarker(marker: Marker?) {
        currentMarker?.remove()
        currentMarker = marker
    }

    fun commitPoi() {
        selectedPOI = currentPOI.value
        selectedPOI?.run {
            longitude.value = latLng.longitude
            latitude.value = latLng.latitude
            reminderSelectedLocationStr.value = name
        }
        isPoiCommitted = true
    }

    fun clearCurrentPoi() {
        if (!isPoiCommitted) {
            currentPOI.value = null
        } else {
            isPoiCommitted = false
        }
    }

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = ""
        reminderDescription.value = ""
        reminderSelectedLocationStr.value = ""
        currentPOI.value = null
        latitude.value = null
        longitude.value = null
        currentMarker = null
        selectedPOI = null
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    suspend fun validateAndSaveReminder(reminderData: ReminderDataItem): Boolean {
        if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
            return true
        }
        return false
    }

    /**
     * Save the reminder to the data source
     */
    suspend fun saveReminder(reminderData: ReminderDataItem) {
        loading {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )
            )
        }
        showToast.value = getApplication<Application>().getString(R.string.reminder_saved)
        navigationCommand.value = NavigationCommand.Back
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }
}