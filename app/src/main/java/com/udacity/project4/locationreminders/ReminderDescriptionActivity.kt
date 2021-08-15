package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )

        val reminder: ReminderDataItem? =
            intent.extras?.getSerializable(EXTRA_ReminderDataItem) as? ReminderDataItem

        if (reminder != null) {
            binding.reminderDataItem = reminder
            binding.map.run {
                visibility = View.VISIBLE
                getMapAsync { googleMap ->
                    googleMap.uiSettings.run {
                        setAllGesturesEnabled(false)
                    }
                    reminder.latitude?.let { lat ->
                        reminder.longitude?.let { lng ->
                            val placeLatLng = LatLng(lat, lng)
                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(placeLatLng)
                                    .title(reminder.title)
                                    .icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                            BitmapDescriptorFactory.HUE_BLUE
                                        )
                                    )
                            )
                            googleMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    placeLatLng,
                                    15F
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
