package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.base.BaseTest
import com.udacity.project4.base.DataBindingViewHolder
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.get
import org.koin.core.component.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

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
    fun clickReminderItem_navigateToReminderDescriptionActivity() {
        runBlocking {
            repository.saveReminders(*reminders.toTypedArray())
            Intents.init()
            launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.AppTheme)

            //WHEN
            val lastReminder = reminders.lastIndex
            onView(withId(R.id.reminderssRecyclerView))
                .check(matches(ViewMatchers.isDisplayed()))
                .check(RecyclerViewItemCountAssertion(reminders.size))
                .perform(
                    RecyclerViewActions.actionOnItemAtPosition<DataBindingViewHolder<ReminderDTO>>(
                        lastReminder,
                        click()
                    )
                )

            //THEN
            intended(hasComponent(ReminderDescriptionActivity::class.java.name))
            Intents.release()
        }
    }
}