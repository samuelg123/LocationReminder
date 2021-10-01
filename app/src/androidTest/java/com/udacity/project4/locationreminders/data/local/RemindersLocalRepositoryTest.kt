package com.udacity.project4.locationreminders.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.base.BaseTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.util.generateDescription
import com.udacity.project4.util.generatePlace
import com.udacity.project4.util.generateRandomCoordinates
import com.udacity.project4.util.generateTitle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.get
import org.koin.test.inject
import kotlin.test.assertFails

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest : BaseTest() {

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

    // runBlocking used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    // TODO replace with runBlockingTest once issue is resolved
    @Test
    fun getInvalidReminderId_reminderIsNotFound() {
        runBlocking {
            //GIVEN
            val invalidId = "thisIsInvalidID"

            //WHEN
            val reminderResult = repository.getReminder(invalidId)

            //THEN
            when (reminderResult) {
                is Result.Error -> assertThat(reminderResult.message).isEqualTo("Reminder not found!")
                is Result.Success -> assertFails("Incorrect. Reminder found!") {}
            }
        }
    }

    @Test
    fun saveReminders_reminderByIdIsEqualToInput() {
        runBlocking {
            //GIVEN
            val input = reminders.first()

            //WHEN
            repository.saveReminder(input)

            //THEN
            when (val reminderResult = repository.getReminder(input.id)) {
                is Result.Error -> assertFails(reminderResult.message) { }
                is Result.Success -> reminderResult.data.run {
                    assertThat(id).isEqualTo(input.id)
                    assertThat(title).isEqualTo(input.title)
                    assertThat(description).isEqualTo(input.description)
                    assertThat(latitude).isEqualTo(input.latitude)
                    assertThat(longitude).isEqualTo(input.longitude)
                }
            }
        }
    }

    @Test
    fun deleteReminders_remindersIsEmpty() {
        runBlocking {
            //GIVEN
            repository.saveReminders(*reminders.toTypedArray())

            //WHEN
            repository.deleteAllReminders()

            //THEN
            when (val reminderResults = repository.getReminders()) {
                is Result.Error -> assertFails(reminderResults.message) { }
                is Result.Success -> reminderResults.data.run {
                    assertThat(this).isEmpty()
                }
            }
        }

    }
}