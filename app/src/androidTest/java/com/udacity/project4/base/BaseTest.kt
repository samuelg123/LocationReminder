package com.udacity.project4.base

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.udacity.project4.modules.loadKoinTestModules
import org.junit.Before
import org.junit.Rule
import org.koin.test.AutoCloseKoinTest

open class BaseTest : AutoCloseKoinTest() {

    protected val app: Application = ApplicationProvider.getApplicationContext()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun beforeSetup() {
        loadKoinTestModules(app)
    }

}