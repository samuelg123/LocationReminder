package com.udacity.project4.base

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.IntentSender
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Base Fragment to observe on the common LiveData objects
 */
abstract class BaseFragment : Fragment() {
    /**
     * Every fragment has to have an instance of a view model that extends from the BaseViewModel
     */
    abstract val _viewModel: BaseViewModel

    override fun onStart() {
        super.onStart()
        _viewModel.showErrorMessage.observe(this, {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        })
        _viewModel.showToast.observe(this, {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        })
        _viewModel.showSnackBar.observe(this, {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_LONG).show()
        })
        _viewModel.showSnackBarInt.observe(this, {
            Snackbar.make(requireView(), getString(it), Snackbar.LENGTH_LONG).show()
        })

        _viewModel.navigationCommand.observe(this, { command ->
            when (command) {
                is NavigationCommand.To -> findNavController().navigate(command.directions)
                is NavigationCommand.Back -> findNavController().popBackStack()
                is NavigationCommand.BackTo -> findNavController().popBackStack(
                    command.destinationId,
                    false
                )
            }
        })
    }

    private val resolutionForResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                resolutionRequestCompletable.complete(true)
                //startLocationUpdates() or do whatever you want
            } else {
                resolutionRequestCompletable.complete(false)
            }
        }

    private val permissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            for ((_, isGranted) in result) {
                if (!isGranted) {
                    permissionRequestCompletable.complete(false)
                    return@registerForActivityResult
                }
            }
            permissionRequestCompletable.complete(true)
        }

    private lateinit var permissionRequestCompletable: CompletableDeferred<Boolean>
    private lateinit var resolutionRequestCompletable: CompletableDeferred<Boolean>
    private val mutex = Mutex()

    suspend fun reqPermissions(vararg permissions: String): Boolean = mutex.withLock {
        permissionRequestCompletable = CompletableDeferred()
        permissionResultLauncher.launch(arrayOf(*permissions))
        return permissionRequestCompletable.await()
    }

    suspend fun Status.reqGps(): Boolean = mutex.withLock {
        return@withLock this.resolution?.let {
            resolutionRequestCompletable = CompletableDeferred()
            return try {
                val intentSenderRequest = IntentSenderRequest.Builder(it).build()
                resolutionForResult.launch(intentSenderRequest)
                resolutionRequestCompletable.await()
            } catch (e: IntentSender.SendIntentException) {
                // Ignore the error.
                false
            } catch (e: ClassCastException) {
                // Ignore, should be an impossible error.
                false
            }
        } ?: false
    }
}