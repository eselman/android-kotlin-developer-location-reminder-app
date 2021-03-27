package com.eselman.locationreminderapp.authentication

import android.app.Application
import androidx.lifecycle.map
import com.eselman.locationreminderapp.base.BaseViewModel

class AuthenticationViewModel(app: Application): BaseViewModel(app) {
    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }
}