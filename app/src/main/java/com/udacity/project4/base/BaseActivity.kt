package com.udacity.project4.utils

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.udacity.project4.R

abstract class BaseNavActivity : AppCompatActivity() {

    @get:IdRes
    abstract val navHostId : Int

    protected val navHostFragment by lazy { supportFragmentManager.findFragmentById(navHostId) as NavHostFragment }
    protected val navController get() = navHostFragment.navController

}