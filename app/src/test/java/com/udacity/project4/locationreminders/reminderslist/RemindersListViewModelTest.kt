package com.udacity.project4.locationreminders.reminderslist

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.base.BaseTest
import com.udacity.project4.locationreminders.data.ReminderFakeRepository
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.util.generateRandomCoordinates
import com.udacity.project4.locationreminders.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RemindersListViewModelTest : BaseTest() {

    //GIVEN
    private val remindersRepository by inject<ReminderFakeRepository>()
    private val remindersListViewModel by inject<RemindersListViewModel>()

    @Before
    fun setup() {
        remindersRepository.addReminders(
            *Array(10) {
                val coordinates = generateRandomCoordinates()
                ReminderDTO(
                    title = faker.book.title(),
                    description = faker.lorem.words(),
                    location = faker.company.name(),
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