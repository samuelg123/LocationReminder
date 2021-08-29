package com.udacity.project4

import android.app.Activity
import android.app.Application
import android.graphics.Rect
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.base.DataBindingViewHolder
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.SelectLocationFragment
import com.udacity.project4.modules.loadKoinTestModules
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.RecyclerViewItemCountAssertion
import com.udacity.project4.util.atPosition
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import java.util.*


@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18) // Minimum SDK supported by UI Automator
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private val app: Application = ApplicationProvider.getApplicationContext()
    private lateinit var device: UiDevice
    private lateinit var repository: RemindersLocalRepository
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        loadKoinTestModules(app)
        // Initialize UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


//    TODO: add End to End testing to the app

    //https://stackoverflow.com/questions/29924564/using-espresso-to-unit-test-google-maps
    @Test
    fun addNewReminder() {
        runBlocking {
            // start up Reminder screen
            val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
            dataBindingIdlingResource.monitorActivity(activityScenario)

            val reminderTitleInput = "Title 123"

            // Add new Reminder data
            onView(withId(R.id.addReminderFAB)).perform(click())
            onView(withId(R.id.reminderTitle))
                .perform(typeText(reminderTitleInput), closeSoftKeyboard())
            onView(withId(R.id.reminderDescription))
                .perform(typeText("DESCRIPTION 123"))

            hideKeyboard()

            // Select location
            onView(withId(R.id.selectLocation)).perform(click())

            val latLng = LatLng(
                37.42225729384123,
                -122.08391804091225
            )
            var selectLocationFragment: SelectLocationFragment? = null
            activityScenario.onActivity {
                val navHostFragment: NavHostFragment = it.navHostFragment
                selectLocationFragment =
                    navHostFragment.childFragmentManager.fragments.first() as SelectLocationFragment
            }
            selectLocationFragment?.run {
                isMapReady.await()
                withContext(Dispatchers.Main) {
                    onClickPoi(
                        PointOfInterest(
                            latLng,
                            UUID.randomUUID().toString(),
                            "Googleplex"
                        )
                    ) // To set POI marker
                }
            }

            //Unused
//        activityScenario.onActivity { reminderActivity ->
//            val latLng = LatLng(
//                37.42225729384123,
//                -122.08391804091225
//            )
//
//            val navHostFragment: NavHostFragment =
//                reminderActivity.navHostFragment //it.supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
//            val selectLocationFragment: SelectLocationFragment =
//                navHostFragment.childFragmentManager.fragments.first() as SelectLocationFragment
//            val map = selectLocationFragment.googleMap
//            map.animateCamera(CameraUpdateFactory.newLatLng(latLng)) // Googleplex coordinates
//
//            onView(isRoot()).perform(waitFor(4000)) // delay load maps
//
//            val tv = TypedValue()
//            val actionBarHeight: Int =
//                if (reminderActivity.theme.resolveAttribute(
//                        android.R.attr.actionBarSize,
//                        tv,
//                        true
//                    )
//                ) {
//                    TypedValue.complexToDimensionPixelSize(
//                        tv.data,
//                        reminderActivity.resources.displayMetrics
//                    )
//                } else {
//                    throw Exception()
//                }
//            val statusBarHeight: Int = reminderActivity.getStatusBarHeight()
//
//            val screenPosition: Point = map.projection.toScreenLocation(latLng)
//            val downTime = SystemClock.uptimeMillis()
//            val eventTime = SystemClock.uptimeMillis() + 100
//            val screenPositionX = screenPosition.x
//            val screenPositionY = screenPosition.y + actionBarHeight + statusBarHeight
//
//            println("$screenPositionX - $screenPositionY")
//            val motionEvent = MotionEvent.obtain(
//                downTime,
//                eventTime,
//                MotionEvent.ACTION_UP,
//                screenPositionX.toFloat(),
//                screenPositionY.toFloat(),
//                0
//            )
//            val v = selectLocationFragment.mapFragment.view
//
////            val success = device.findObject(
////                UiSelector()
////                    .className("com.google.android.gms.maps.SupportMapFragment")
////            ).click()
////            if (!success) throw assertFails {  }
////            device.click(screenPositionX, screenPositionY)
////            device.drag(screenPositionX, screenPositionY,screenPositionX, screenPositionY, 1000)
////
////            device.waitForIdle(1000)
//            device.swipe(screenPositionX, screenPositionY-10, screenPositionX, screenPositionY+10, 200)
//            v!!.dispatchTouchEvent(motionEvent)
//
//            onView(isRoot()).perform(waitFor(4000)) // delay load maps
//        }

            onView(withId(R.id.save)).perform(click())

            onView(withId(R.id.saveReminder))
                .check(matches(isDisplayed()))
                .perform(click())

            // Verify it was deleted
            onView(withId(R.id.reminderssRecyclerView))
                .check(matches(isDisplayed()))
                .check(RecyclerViewItemCountAssertion(1))
                .perform(
                    RecyclerViewActions.actionOnItemAtPosition<DataBindingViewHolder<ReminderDTO>>(
                        0,
                        click()
                    )
                )

            device.pressBack()

            onView(withId(R.id.reminderssRecyclerView))
                .check(matches(atPosition(0, hasDescendant(withText(reminderTitleInput)))))

            // Make sure the activity is closed before resetting the db:
            activityScenario.close()
        }
    }

    private fun hideKeyboard(): ViewInteraction =
        onView(isRoot()).perform(closeSoftKeyboard())

    fun Activity.getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId)
        else Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.top
    }
}
