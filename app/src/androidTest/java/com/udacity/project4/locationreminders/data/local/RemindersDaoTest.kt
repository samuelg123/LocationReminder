package com.udacity.project4.locationreminders.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.base.BaseTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.util.generateDescription
import com.udacity.project4.util.generatePlace
import com.udacity.project4.util.generateRandomCoordinates
import com.udacity.project4.util.generateTitle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.get
import org.koin.test.inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
open class RemindersDaoTest : BaseTest() {

    private val reminderDao: RemindersDao by inject()
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
    fun saveReminders_getReminderByIdEqualToInput() = runBlockingTest {
        val input = reminders.first()
        reminderDao.saveReminder(input)

        val reminderResult = reminderDao.getReminderById(input.id)

        assertThat(reminderResult).isNotNull()
        reminderResult?.run {
            assertThat(id).isEqualTo(input.id)
            assertThat(title).isEqualTo(input.title)
            assertThat(description).isEqualTo(input.description)
            assertThat(latitude).isEqualTo(input.latitude)
            assertThat(longitude).isEqualTo(input.longitude)
        }
    }

    @Test
    fun saveReminders_getRemindersIsNotEmpty() = runBlockingTest {
        reminderDao.saveReminders(*reminders.toTypedArray())

        val reminderResult = reminderDao.getReminders()

        assertThat(reminderResult).isNotEmpty()
        assertThat(reminderResult.size).isEqualTo(reminders.size)
    }

    @Test
    fun deleteReminders_getRemindersIsEmpty() = runBlockingTest {
        reminderDao.saveReminders(*reminders.toTypedArray())

        reminderDao.deleteAllReminders()
        val reminderResults = reminderDao.getReminders()

        assertThat(reminderResults).isEmpty()
    }

}