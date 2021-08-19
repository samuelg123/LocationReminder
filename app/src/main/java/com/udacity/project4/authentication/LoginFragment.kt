package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.authentication.LoginViewModel.AuthenticationState.*
import com.udacity.project4.databinding.LoginFragmentBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.utils.toast

class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModel()

    private lateinit var binding: LoginFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoginFragmentBinding.inflate(inflater)
        binding.fragment = this
        binding.lifecycleOwner = this

        binding.googleSigninButton.setOnClickListener {
            signInWithGoogle()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.authenticationState.observe(viewLifecycleOwner) {
            when (it) {
                AUTHENTICATED -> gotoReminder()
                INVALID_AUTHENTICATION -> {
                    requireContext().toast(R.string.invalid_auth_message)
                }
                UNAUTHENTICATED -> {
                    // no op
                }
                else -> {
                    // no op
                }
            }
        }
    }

    fun signInWithEmail(view: View) {
        val providers = arrayListOf(
            AuthUI
                .IdpConfig
                .EmailBuilder()
                .build()
        )

        // Create and launch sign-in intent.
        // We listen to the response of this activity with the
        // SIGN_IN_RESULT_CODE code
        startActivityForResult(
            AuthUI
                .getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_RESULT_CODE
        )
    }

    fun signInWithGoogle() {
        val providers = arrayListOf(
            AuthUI.IdpConfig
                .GoogleBuilder()
                .build()
        )

        // Create and launch sign-in intent.
        // We listen to the response of this activity with the
        // SIGN_IN_RESULT_CODE code
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            SIGN_IN_RESULT_CODE
        )
    }

    private fun gotoReminder() {
        val intent = Intent(requireActivity(), RemindersActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                Log.i(
                    TAG,
                    "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

    companion object {
        const val TAG = "LoginFragment"
        const val SIGN_IN_RESULT_CODE = 1001
    }

}