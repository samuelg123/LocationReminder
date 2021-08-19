package com.udacity.project4.locationreminders

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.udacity.project4.locationreminders.modules.remindersListModule
import com.udacity.project4.modules.dataModule
import com.udacity.project4.modules.viewModelModule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.koin.android.ext.koin.androidContext
import org.koin.test.AutoCloseKoinTest
import org.koin.test.category.CheckModuleTest
import org.koin.test.check.checkModules
import org.robolectric.annotation.Config

/**
 * Dry run configuration
 */
@Category(CheckModuleTest::class)
class ModuleCheckTest : AutoCloseKoinTest() {

    @Test
    fun checkModules() = checkModules {
        modules(remindersListModule)
    }
}