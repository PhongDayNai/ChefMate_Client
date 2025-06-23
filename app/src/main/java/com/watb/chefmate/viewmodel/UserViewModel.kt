package com.watb.chefmate.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun isLoggedIn(context: Context) {
        viewModelScope.launch {
            _isLoggedIn.value = DataStoreHelper.isLoggedIn(context)
            if (_isLoggedIn.value) {
                getUserData(context)
            }
        }
    }

    fun saveLoginState(context: Context, userData: UserData) {
        viewModelScope.launch {
            DataStoreHelper.saveLoginState(
                context = context,
                isLoggedIn = true,
                userId = userData.userId,
                username = userData.fullName,
                email = userData.email,
                phoneNumber = userData.phone,
                followCount = userData.followCount,
                recipeCount = userData.recipeCount,
                createdAt = userData.createdAt
            )
            isLoggedIn(context)
        }
    }

    fun getUserData(context: Context) {
        viewModelScope.launch {
            _user.value = DataStoreHelper.getUserData(context)
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            DataStoreHelper.clearLoginState(context)
            _isLoggedIn.value = false
            _user.value = null
        }
    }
}