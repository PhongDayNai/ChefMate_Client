package com.watb.chefmate.api

import android.content.Context
import com.google.gson.Gson
import com.watb.chefmate.data.AuthSessionPayload
import com.watb.chefmate.data.RefreshTokenRequest
import com.watb.chefmate.data.UserData
import com.watb.chefmate.helper.ChatSnapshotStore
import com.watb.chefmate.helper.DataStoreHelper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object SessionRepository {
    private val gson = Gson()
    private val refreshMutex = Mutex()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private lateinit var appContext: Context
    private lateinit var secureTokenStore: SecureTokenStore

    fun init(context: Context) {
        if (::secureTokenStore.isInitialized) return
        appContext = context.applicationContext
        secureTokenStore = SecureTokenStore(appContext)
    }

    suspend fun isLoggedIn(): Boolean {
        ensureInitialized()
        return !getAccessToken().isNullOrBlank() && DataStoreHelper.isLoggedIn(appContext)
    }

    suspend fun saveAuthenticatedSession(session: AuthSessionPayload) {
        ensureInitialized()
        secureTokenStore.saveTokens(
            accessToken = session.accessToken,
            refreshToken = session.refreshToken
        )
        DataStoreHelper.saveUserProfile(appContext, session.user, isLoggedIn = true)
    }

    suspend fun updateUserProfile(userData: UserData) {
        ensureInitialized()
        DataStoreHelper.saveUserProfile(appContext, userData, isLoggedIn = true)
    }

    suspend fun getAccessToken(): String? {
        ensureInitialized()
        return secureTokenStore.getAccessToken()
    }

    suspend fun getRefreshToken(): String? {
        ensureInitialized()
        return secureTokenStore.getRefreshToken()
    }

    suspend fun clearSession() {
        ensureInitialized()
        val existingUserId = runCatching { DataStoreHelper.getUserId(appContext) }.getOrNull()
        secureTokenStore.clear()
        DataStoreHelper.clearLoginState(appContext)
        if (existingUserId != null && existingUserId > 0) {
            ChatSnapshotStore.clearUser(existingUserId)
        } else {
            ChatSnapshotStore.clearAll()
        }
    }

    suspend fun refreshSession(): Boolean = refreshMutex.withLock {
        ensureInitialized()
        val refreshToken = secureTokenStore.getRefreshToken().orEmpty()
        if (refreshToken.isBlank()) {
            clearSession()
            return@withLock false
        }

        val requestBody = gson.toJson(RefreshTokenRequest(refreshToken))
            .toRequestBody(jsonMediaType)

        val result = ApiRequestExecutor.executeRaw(AuthMode.PUBLIC) {
            Request.Builder()
                .url(ApiConstant.REFRESH_TOKEN_URL)
                .post(requestBody)
                .build()
        }

        val authResponse = parseAuthResponse(result)
        val session = authResponse.data
        if (result.success && session != null) {
            saveAuthenticatedSession(session)
            true
        } else {
            clearSession()
            false
        }
    }

    private fun ensureInitialized() {
        check(::secureTokenStore.isInitialized) {
            "SessionRepository.init(context) must be called before use"
        }
    }
}
