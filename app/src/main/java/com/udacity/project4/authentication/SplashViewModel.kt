package com.udacity.project4.authentication

import android.app.Application
import androidx.lifecycle.asFlow
import androidx.lifecycle.asLiveData
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.utils.FirebaseUserLiveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map

class SplashViewModel(app: Application) : BaseViewModel(app) {
//    sealed class Event {
//        object CheckPreferences : Event()
//    }
//
//    sealed class State {
//        data class UserAvailable(val user: FirebaseUser) : State()
//        object UserUnavailable : State()
//    }
//
//    val state: LiveData<State>
//        get() = _state
//    private val _state = MutableLiveData<State>()
//
//    val authenticationState = FirebaseUserLiveData().map { user ->
//        if (user != null) {
//            setState(State.UserAvailable())
//        } else {
//            setState(State.UserAvailable("ok"))
//        }
//    }
//
//    private fun setState(newState: State) {
//        _state.value = newState
//    }

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    val authenticationState = FirebaseUserLiveData().asFlow().map { user ->
        delay(1200)
        if (user != null) AuthenticationState.AUTHENTICATED
        else AuthenticationState.UNAUTHENTICATED
    }.asLiveData()
}