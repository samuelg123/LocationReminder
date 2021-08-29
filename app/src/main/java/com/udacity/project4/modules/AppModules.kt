package com.udacity.project4.modules

import androidx.annotation.VisibleForTesting
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

@VisibleForTesting
object AppModules {

    val presentation = module {
        //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
        viewModel {
            SplashViewModel(get())
        }
        viewModel {
            LoginViewModel(get())
        }
        viewModel {
            RemindersListViewModel(get(), get<RemindersLocalRepository>())
        }
        viewModel {
            SaveReminderViewModel(get(), get<RemindersLocalRepository>())
        }
        viewModel {
            RemindersViewModel(get())
        }
    }

    val data = module {
        //Declare singleton definitions to be later injected using by inject()
        single { RemindersLocalRepository(get()) }
        single { LocalDB.createRemindersDao(androidContext()) }
    }

}
