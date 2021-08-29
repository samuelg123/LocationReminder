package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.base.BaseTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.get
import org.koin.core.component.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.EnumSet.allOf

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : BaseTest() {

    private val repository: RemindersLocalRepository by inject()
    private lateinit var reminders: List<ReminderDTO>

    @Before
    fun setup() {
        reminders = listOf(
            *Array(10) {
                val coordinates = generateRandomCoordinates()
                ReminderDTO(
                    title = generateTitle(),
                    description = generateDescription(),
                    location = generatePlace(),
                    latitude = coordinates.latitude,
                    longitude = coordinates.longitude,
                )
            }
        )
    }

    @After
    fun tearDown() = get<RemindersDatabase>().close()


    @Test
    fun clickAddReminderFAB_navigateToSaveReminderFragment() {
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //WHEN
        onView(withId(R.id.addReminderFAB)).check(matches(ViewMatchers.isDisplayed()))
        onView(withId(R.id.addReminderFAB)).perform(click())

        //THEN
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun saveReminders_showNewData() {
        runBlocking {
            //WHEN
            repository.saveReminders(*reminders.toTypedArray())
            launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.AppTheme)

            //THEN
            onView(withId(R.id.reminderssRecyclerView))
                .check(matches(ViewMatchers.isDisplayed()))
                .check(RecyclerViewItemCountAssertion(reminders.size))
        }
    }
}