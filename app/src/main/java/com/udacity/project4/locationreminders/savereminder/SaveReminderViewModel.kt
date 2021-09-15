package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.Marker
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

class SaveReminderViewModel(
    app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {
    val reminderDataItem = MutableLiveData(ReminderDataItem())

    //Google Map
    val tempDataItem = MutableLiveData<ReminderDataItem?>()
    private var tempMarker: Marker? = null
    var isMapLocationSaved: Boolean = false

    fun loadReminderDataItem() {
        tempDataItem.value = reminderDataItem.value
    }

    fun setMarker(marker: Marker?) {
        tempMarker?.remove()
        tempMarker = marker
    }

    fun commitLocation() {
        reminderDataItem.value = tempDataItem.value
        isMapLocationSaved = true
    }

    fun clearCurrentLocation() {
        if (!isMapLocationSaved) {
            tempDataItem.value = null
        } else {
            isMapLocationSaved = false
        }
    }

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderDataItem.value = ReminderDataItem()
        tempDataItem.value = null
        tempMarker = null
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