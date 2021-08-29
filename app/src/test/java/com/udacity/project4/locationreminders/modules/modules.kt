package com.udacity.project4.locationreminders.modules

import android.app.Application
import androidx.annotation.VisibleForTesting
import com.udacity.project4.locationreminders.data.ReminderFakeDataSource
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module


fun loadKoinTestModules(app: Application) = loadKoinModules(module {
    viewModel {
        RemindersListViewModel(app, get<ReminderFakeDataSource>())
    }
    viewModel {
        SaveReminderViewModel(app, get<ReminderFakeDataSource>())
    }
    single { ReminderFakeDataSource() }
})