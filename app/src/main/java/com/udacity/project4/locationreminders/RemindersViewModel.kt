package com.udacity.project4.locationreminders

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.withContext

class RemindersViewModel(app: Application) : BaseViewModel(app) {
    sealed class Event {
        object Logout : Event()
    }

    sealed class State {
        object LogoutSuccess : State()
    }

    private val _state = MutableLiveData<State>()
    val state: LiveData<State>
        get() = _state

    fun onEvent(event: Event) = when (event) {
        Event.Logout -> logout()
    }

    private fun logout() = viewModelScope.launch {
        loading {
            withContext(Dispatchers.IO) {
                AuthUI.getInstance().signOut(getApplication()).asDeferred().await()
            }
        }
        setState(State.LogoutSuccess)
    }

    private fun setState(state: State) {
        _state.value = state
    }
}