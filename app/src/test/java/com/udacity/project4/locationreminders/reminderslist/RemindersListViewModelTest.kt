package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.ReminderFakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.modules.remindersListModule
import com.udacity.project4.locationreminders.util.generateRandomCoordinates
import com.udacity.project4.locationreminders.util.getOrAwaitValue
import io.github.serpro69.kfaker.Faker
import io.github.serpro69.kfaker.faker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RemindersListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger()
        modules(remindersListModule)
    }
//
//    private val faker by inject<Faker>()
//    private val remindersDataSource by inject<ReminderFakeDataSource>()
//    private val remindersListViewModel by inject<RemindersListViewModel>()

//    @Before
//    fun setupDataSource() {
//        remindersDataSource.addReminders(
//            *Array(10) {
//                val coordinates = generateRandomCoordinates()
//                ReminderDTO(
//                    title = faker.book.title(),
//                    description = faker.lorem.words(),
//                    location = faker.company.name(),
//                    latitude = coordinates.latitude,
//                    longitude = coordinates.longitude,
//                )
//            }
//        )
//    }

    @Test
    fun `WHEN load reminders THEN value is not empty`() {
//        // When adding a new task
//        remindersListViewModel.loadReminders()
//
//        // Then the new task event is triggered
//        val value = remindersListViewModel.remindersList.getOrAwaitValue()
//
//        println("Total: $value reminders")
//
//        assertThat(value).isNotEmpty()
    }

//    @After
//    fun tearDown(){
//        stopKoin()
//    }
}