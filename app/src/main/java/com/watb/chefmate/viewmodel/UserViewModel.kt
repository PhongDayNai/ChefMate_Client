package com.watb.chefmate.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watb.chefmate.api.SessionRepository
import com.watb.chefmate.data.AuthSessionPayload
import com.watb.chefmate.data.UserData
import com.watb.chefmate.helper.DataStoreHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _user = MutableStateFlow<UserData?>(null)
    val user: StateFlow<UserData?> = _user

    fun isLoggedIn(context: Context, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoggedIn.value = SessionRepository.isLoggedIn()
            if (_isLoggedIn.value) {
                getUserData(context) {
                    onFinished()
                }
            } else {
                _user.value = null
            }
        }
    }

    fun saveLoginState(context: Context, userData: UserData, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            DataStoreHelper.saveUserProfile(context, userData, isLoggedIn = true)
            isLoggedIn(context) {
                onFinished()
            }
        }
    }

    fun saveAuthenticatedSession(
        context: Context,
        session: AuthSessionPayload,
        onFinished: () -> Unit = {}
    ) {
        viewModelScope.launch {
            SessionRepository.saveAuthenticatedSession(session)
            _user.value = session.user
            _isLoggedIn.value = true
            onFinished()
        }
    }

    fun getUserData(context: Context, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            _user.value = DataStoreHelper.getUserData(context)
            onFinished()
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            SessionRepository.clearSession()
            _isLoggedIn.value = false
            _user.value = null
        }
    }
}
