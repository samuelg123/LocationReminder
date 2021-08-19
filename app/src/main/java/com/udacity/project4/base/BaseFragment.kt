package com.udacity.project4.base

import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

/**
 * Base Fragment to observe on the common LiveData objects
 */
abstract class BaseFragment<out VM : BaseViewModel> : Fragment() {

    protected abstract val viewModel: VM

    @CallSuper
    override fun onStart() {
        super.onStart()
        viewModel.attachDialogs()
    }

    protected val parentActivity: BaseActivity
        get() {
            val a = activity
            if (a is BaseActivity) return a
            throw Exception("Must be BaseActivity!")
        }

    private fun BaseViewModel.attachDialogs() {
        showErrorMessage.observe(this@BaseFragment) {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        }
        showToast.observe(this@BaseFragment) {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        }
        showSnackBar.observe(this@BaseFragment) {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        }
        showSnackBarInt.observe(this@BaseFragment) {
            Snackbar.make(requireView(), getString(it), Snackbar.LENGTH_LONG).show()
        }

        navigationCommand.observe(this@BaseFragment) { command ->
            when (command) {
                is NavigationCommand.To -> findNavController().navigate(
                    command.directions
                )
                is NavigationCommand.Back -> findNavController().popBackStack()
                is NavigationCommand.BackTo -> findNavController().popBackStack(
                    command.destinationId,
                    false
                )
            }
        }
    }
}