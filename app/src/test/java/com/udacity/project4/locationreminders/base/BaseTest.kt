package com.udacity.project4.locationreminders.base

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.modules.loadKoinTestModules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.koin.test.AutoCloseKoinTest

open class BaseTest : AutoCloseKoinTest() {

    private val app: Application = ApplicationProvider.getApplicationContext()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun beforeSetup() {
        loadKoinTestModules(app)
    }

}