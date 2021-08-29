package com.udacity.project4.modules

import android.app.Application
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

fun provideDatabase(app: Application): RemindersDatabase =
    Room.inMemoryDatabaseBuilder(
        app,
        RemindersDatabase::class.java
    ).build()

fun loadKoinTestModules(app: Application) = loadKoinModules(module {
    viewModel {
        RemindersListViewModel(
            app,
            get<RemindersLocalRepository>()
        )
    }
    viewModel {
        SaveReminderViewModel(
            app,
            get<RemindersLocalRepository>()
        )
    }
    single { provideDatabase(app) }
    single { get<RemindersDatabase>().reminderDao() } // ReminderDao
    single {
        RemindersLocalRepository(get())
    }
})