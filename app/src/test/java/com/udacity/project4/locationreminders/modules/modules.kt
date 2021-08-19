package com.udacity.project4.locationreminders.modules

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.udacity.project4.locationreminders.data.ReminderFakeDataSource
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import io.github.serpro69.kfaker.faker
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val remindersListModule = module {
    single {
        faker {}
    }
    viewModel {
        RemindersListViewModel(androidApplication(), get())
    }
    single { ReminderFakeDataSource() }
}
