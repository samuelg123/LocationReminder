package com.udacity.project4.locationreminders.savereminder

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.base.BaseTest
import com.udacity.project4.locationreminders.data.ReminderFakeRepository
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.util.generateRandomCoordinates
import com.udacity.project4.locationreminders.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest : BaseTest() {

    //GIVEN
    private val remindersRepository by inject<ReminderFakeRepository>()
    private val saveReminderViewModel by inject<SaveReminderViewModel>()

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
    fun `GIVEN correct data WHEN validate entered data THEN result is true`() {
        // GIVEN correct data
        val coordinates = generateRandomCoordinates()
        val data = ReminderDataItem(
            title = faker.book.title(),
            description = faker.lorem.words(),
            location = faker.company.name(),
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
        )

        // WHEN validate entered data
        val result = saveReminderViewModel.validateEnteredData(data)

        // THEN result is true
        assertThat(result).isTrue()
    }

    @Test
    fun `GIVEN incorrect data WHEN validate entered data THEN result is false`() {
        // GIVEN incorrect data
        val data = ReminderDataItem(
            title = null,
            description = null,
            location = faker.company.name(),
            latitude = null,
            longitude = null,
        )

        // WHEN validate entered data
        val result = saveReminderViewModel.validateEnteredData(data)

        // THEN result is false
        assertThat(result).isFalse()
    }

    @Test
    fun `GIVEN input data WHEN save reminder THEN result data is equal to input data`() =
        runBlockingTest {
            // GIVEN input data
            val coordinates = generateRandomCoordinates()
            val input = ReminderDataItem(
                title = faker.book.title(),
                description = faker.lorem.words(),
                location = faker.company.name(),
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
    fun `GIVEN POI data WHEN commit POI THEN coordinates and location name is equal POI data`() {
        // GIVEN data
        val coordinates = generateRandomCoordinates()
        val poi = PointOfInterest(
            LatLng(coordinates.latitude, coordinates.longitude),
            faker.random.nextUUID(),
            faker.company.name(),
        )
        saveReminderViewModel.currentPOI.value = poi

        // WHEN save reminder
        saveReminderViewModel.commitPoi()

        // THEN result data is equal to poi data
        saveReminderViewModel.run {
            assertThat(latitude.getOrAwaitValue()).isEqualTo(poi.latLng.latitude)
            assertThat(longitude.getOrAwaitValue()).isEqualTo(poi.latLng.longitude)
            assertThat(reminderSelectedLocationStr.getOrAwaitValue()).isEqualTo(poi.name)
            assertThat(isPoiCommitted).isTrue()
        }
    }

    @Test
    fun `GIVEN selected data WHEN on clear data THEN data cleared`() {
        // GIVEN selected data
        val coordinates = generateRandomCoordinates()
        val poi = PointOfInterest(
            LatLng(coordinates.latitude, coordinates.longitude),
            faker.random.nextUUID(),
            faker.company.name(),
        )
        val input = ReminderDataItem(
            title = faker.book.title(),
            description = faker.lorem.words(),
            location = faker.company.name(),
            latitude = coordinates.latitude,
            longitude = coordinates.longitude,
        )
        saveReminderViewModel.apply {
            reminderTitle.value = input.title
            reminderDescription.value = input.description
            reminderSelectedLocationStr.value = input.location
            currentPOI.value = poi
            latitude.value = input.latitude
            longitude.value = input.longitude
        }

        // WHEN on clear data
        saveReminderViewModel.onClear()

        // THEN result data is empty
        saveReminderViewModel.run {
            assertThat(reminderTitle.getOrAwaitValue()).isEmpty()
            assertThat(reminderDescription.getOrAwaitValue()).isEmpty()
            assertThat(reminderSelectedLocationStr.getOrAwaitValue()).isEmpty()
            assertThat(currentPOI.getOrAwaitValue()).isNull()
            assertThat(latitude.getOrAwaitValue()).isNull()
            assertThat(longitude.getOrAwaitValue()).isNull()
        }
    }
}