package com.udacity.project4.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity

class SplashActivity : AppCompatActivity() {
    private val viewModel: SplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        viewModel.authenticationState.observe(this) {
            when (it) {
                SplashViewModel.AuthenticationState.AUTHENTICATED -> gotoReminder()
                else -> gotoIntro()
            }
        }
    }

    private fun gotoReminder() {
        val intent = Intent(this, RemindersActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    private fun gotoIntro() {
        val intent = Intent(this, AuthenticationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

}