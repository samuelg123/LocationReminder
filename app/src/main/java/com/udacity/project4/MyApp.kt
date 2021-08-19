package com.udacity.project4

import android.app.Application
import com.udacity.project4.modules.dataModule
import com.udacity.project4.modules.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

open class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        /**
         * use Koin Library as a service locator
         */
        startKoin {
            androidContext(this@MyApp)
            modules(viewModelModule, dataModule)
        }
    }
}