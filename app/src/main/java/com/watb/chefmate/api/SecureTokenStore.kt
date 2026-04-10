package com.watb.chefmate.api

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureTokenStore(context: Context) {
    private val appContext = context.applicationContext

    private val preferences by lazy {
        createPreferencesWithRecovery()
    }

    fun getAccessToken(): String? = preferences.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = preferences.getString(KEY_REFRESH_TOKEN, null)

    fun saveTokens(accessToken: String, refreshToken: String) {
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private fun createPreferencesWithRecovery() =
        runCatching { createPreferences() }
            .getOrElse { throwable ->
                Log.w(TAG, "Failed to open encrypted session store. Clearing corrupted state.", throwable)
                appContext.deleteSharedPreferences(STORE_NAME)
                createPreferences()
            }

    private fun createPreferences() : android.content.SharedPreferences {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            appContext,
            STORE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private companion object {
        const val TAG = "SecureTokenStore"
        const val STORE_NAME = "chefmate_secure_session"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
