package com.udacity.project4.locationreminders.modules

import android.app.Application
import com.udacity.project4.locationreminders.data.ReminderFakeRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module


fun loadKoinTestModules(app: Application) = loadKoinModules(module {
    viewModel {
        RemindersListViewModel(app, get<ReminderFakeRepository>())
    }
    viewModel {
        SaveReminderViewModel(app, get<ReminderFakeRepository>())
    }
    single { ReminderFakeRepository() }
})