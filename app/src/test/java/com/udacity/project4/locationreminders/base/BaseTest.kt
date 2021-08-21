package com.udacity.project4.locationreminders.base

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.udacity.project4.MyApp
import com.udacity.project4.locationreminders.MainCoroutineScopeRule
import com.udacity.project4.locationreminders.modules.loadKoinTestModules
import io.github.serpro69.kfaker.faker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.koin.test.AutoCloseKoinTest

open class BaseTest : AutoCloseKoinTest() {

    private val app = ApplicationProvider.getApplicationContext<MyApp>()

    protected val faker = faker {}

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineScopeRule = MainCoroutineScopeRule()

    @Before
    fun beforeSetup(){
        loadKoinTestModules(app)
    }

}