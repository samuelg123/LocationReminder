package com.udacity.project4.locationreminders.reminderslist

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.base.BaseTest
import com.udacity.project4.locationreminders.data.ReminderFakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RemindersListViewModelTest : BaseTest() {

    //GIVEN
    private val reminderDataSource by inject<ReminderFakeDataSource>()
    private val remindersListViewModel by inject<RemindersListViewModel>()

    @Before
    fun setup() {
        reminderDataSource.addReminders(
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

    @Test
    fun `WHEN load reminders THEN value is not empty`() {
        // When load reminders
        remindersListViewModel.loadReminders()

        // Then reminders list is not empty
        val value = remindersListViewModel.remindersList.getOrAwaitValue()

        println("Total: ${value.size} reminders")

        assertThat(value).isNotEmpty()
    }

}