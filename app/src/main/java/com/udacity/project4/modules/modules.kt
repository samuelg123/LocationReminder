package com.udacity.project4.modules

import com.udacity.project4.authentication.LoginViewModel
import com.udacity.project4.authentication.SplashViewModel
import com.udacity.project4.locationreminders.RemindersViewModel
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
    viewModel {
        SplashViewModel(get())
    }
    viewModel {
        LoginViewModel(get())
    }
    viewModel {
        RemindersListViewModel(get(), get())
    }
    viewModel {
        SaveReminderViewModel(get(), get())
    }
    viewModel {
        RemindersViewModel(get())
    }
}

val dataModule = module {
    //Declare singleton definitions to be later injected using by inject()
    single { RemindersLocalRepository(get()) }
    single { LocalDB.createRemindersDao(androidContext()) }
}
