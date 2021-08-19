package com.udacity.project4.locationreminders

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.udacity.project4.R
import com.udacity.project4.base.BaseActivity
import com.udacity.project4.databinding.ActivityRemindersBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : BaseActivity() {

    private lateinit var binding: ActivityRemindersBinding

    override val navHostId: Int = R.id.fragment_container

    private val viewModel: RemindersViewModel by viewModel()

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminders)
        binding.lifecycleOwner = this

        appBarConfiguration = AppBarConfiguration(navController.graph)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }
}
