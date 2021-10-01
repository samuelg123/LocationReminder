package com.udacity.project4.locationreminders.savereminder

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.R
import com.udacity.project4.locationreminders.base.BaseTest
import com.udacity.project4.locationreminders.data.ReminderFakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.inject
import kotlin.test.assertFails

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest : BaseTest() {

    //GIVEN
    private val remindersRepository by inject<ReminderFakeDataSource>()
    private val saveReminderViewModel by inject<SaveReminderViewModel>()

    @Before
    fun setup() {
        remindersRepository.addReminders(
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
    fun `GIVEN correct data WHEN validate entered data THEN result is true`() {
        // GIVEN correct data
        val coordinates = generateRandomCoordinates()
        val data = ReminderDataItem(
            title = generateTitle(),
            description = generateDescription(),
            location = generatePlace(),
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
        )

        // WHEN validate entered data
        val result = saveReminderViewModel.validateEnteredData(data)

        // THEN result is true
        assertThat(result).isTrue()
    }

    @Test
    fun `GIVEN empty title reminder WHEN validate entered data THEN result is false and snackbar shown`() {
        // GIVEN incorrect data
        val data = ReminderDataItem(
            title = null,
            description = null,
            location = generatePlace(),
            latitude = null,
            longitude = null,
        )

        // WHEN validate entered data
        val result = saveReminderViewModel.validateEnteredData(data)

        // THEN result is false and snackbar shown
        assertThat(result).isFalse()
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_enter_title)
    }


    @Test
    fun `GIVEN location not selected WHEN validate entered data THEN result is false and snackbar shown`() {
        // GIVEN incorrect data
        val data = ReminderDataItem(
            title = "This is title 123",
            description = null,
            location = null,
            latitude = null,
            longitude = null,
        )

        // WHEN validate entered data
        val result = saveReminderViewModel.validateEnteredData(data)

        // THEN result is false and snackbar shown
        assertThat(result).isFalse()
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_select_location)
    }

    @Test
    fun `GIVEN input data WHEN save reminder THEN result data is equal to input data`() =
        runBlockingTest {
            // GIVEN input data
            val coordinates = generateRandomCoordinates()
            val input = ReminderDataItem(
                title = generateTitle(),
                description = generateDescription(),
                location = generatePlace(),
                latitude = coordinates.latitude,
                longitude = coordinates.longitude,
            )

            // WHEN save reminder
            saveReminderViewModel.saveReminder(input)

            // THEN result data is not empty
            when (val result = remindersRepository.getReminder(input.id)) {
                is Result.Error -> {
                    //Failed to get the data
                    assertThat(result).isInstanceOf(Result.Success::class.java)
                }
                is Result.Success -> result.data.run {
                    //Successfully get the data
                    assertThat(this.id).isEqualTo(input.id)
                    assertThat(this.title).isEqualTo(input.title)
                    assertThat(this.description).isEqualTo(input.description)
                    assertThat(this.location).isEqualTo(input.location)
                    assertThat(this.latitude).isEqualTo(input.latitude)
                    assertThat(this.longitude).isEqualTo(input.longitude)
                }
            }
        }

    @Test
    fun `GIVEN chosen location data WHEN commit location THEN coordinates and location name is equal to location data`() {
        // GIVEN chosen location data
        val coordinates = generateRandomCoordinates()
        val input = ReminderDataItem(
            title = generateTitle(),
            description = generateDescription(),
            location = generatePlace(),
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
        )
        saveReminderViewModel.tempDataItem.value = input

        // WHEN save reminder
        saveReminderViewModel.commitLocation()

        // THEN result data is equal to chosen location data
        saveReminderViewModel.run {
            reminderDataItem.getOrAwaitValue()?.run {
                assertThat(latitude).isEqualTo(input.latitude)
                assertThat(longitude).isEqualTo(input.longitude)
                assertThat(location).isEqualTo(input.location)
            } ?: assertFails("reminderDataItem is null") {}
            assertThat(isMapLocationSaved).isTrue()
        }
    }

    @Test
    fun `GIVEN selected data WHEN on clear data THEN data cleared`() {
        // GIVEN selected data
        val coordinates = generateRandomCoordinates()
        val input = ReminderDataItem(
            title = generateTitle(),
            description = generateDescription(),
            location = generatePlace(),
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
        )
        saveReminderViewModel.reminderDataItem.value = input

        saveReminderViewModel.loadReminderDataItem()

        // WHEN on clear data
        saveReminderViewModel.onClear()

        // THEN result data is null
        assertThat(saveReminderViewModel.tempDataItem.getOrAwaitValue()).isNull()
    }
}