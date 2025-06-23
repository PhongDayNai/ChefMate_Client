package com.watb.chefmate.helper

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.watb.chefmate.data.UserData
import kotlinx.coroutines.flow.first

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dataStore")
val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
val USER_ID = intPreferencesKey("user_id")
val USERNAME = stringPreferencesKey("username")
val EMAIL = stringPreferencesKey("email")
val PHONE_NUMBER = stringPreferencesKey("phone_number")
val FOLLOW_COUNT = intPreferencesKey("follow_count")
val RECIPE_COUNT = intPreferencesKey("recipe_count")
val CREATED_AT = stringPreferencesKey("created_at")

val LAST_SHOPPING_ID = intPreferencesKey("last_shopping_id")
val IS_FINISHED_SHOPPING = booleanPreferencesKey("is_finished_shopping")

object DataStoreHelper {
    suspend fun isLoggedIn(context: Context): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[IS_LOGGED_IN] ?: false
    }

    suspend fun saveLoginState(
        context: Context,
        isLoggedIn: Boolean,
        userId: Int,
        username: String,
        email: String,
        phoneNumber: String,
        followCount: Int,
        recipeCount: Int,
        createdAt: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = isLoggedIn
            preferences[USER_ID] = userId
            preferences[USERNAME] = username
            preferences[EMAIL] = email
            preferences[PHONE_NUMBER] = phoneNumber
            preferences[FOLLOW_COUNT] = followCount
            preferences[RECIPE_COUNT] = recipeCount
            preferences[CREATED_AT] = createdAt
        }
    }

    suspend fun getUserData(context: Context): UserData {
        val preferences = context.dataStore.data.first()
        return UserData(
            userId = preferences[USER_ID] ?: 0,
            fullName = preferences[USERNAME] ?: "User",
            phone = preferences[PHONE_NUMBER] ?: "0123456789",
            preferences[EMAIL] ?: "user@gmail.com",
            followCount = preferences[FOLLOW_COUNT] ?: 0,
            recipeCount = preferences[RECIPE_COUNT] ?: 0,
            createdAt = preferences[CREATED_AT] ?: "2025-06-26"
        )
    }

    suspend fun getUserId(context: Context): Int {
        val preferences = context.dataStore.data.first()
        return preferences[USER_ID] ?: 0
    }

    suspend fun getUsername(context: Context): String {
        val preferences = context.dataStore.data.first()
        return preferences[USERNAME] ?: ""
    }

    suspend fun getEmail(context: Context): String {
        val preferences = context.dataStore.data.first()
        return preferences[EMAIL] ?: ""
    }

    suspend fun getPhoneNumber(context: Context): String {
        val preferences = context.dataStore.data.first()
        return preferences[PHONE_NUMBER] ?: ""
    }

    suspend fun getFollowCount(context: Context): Int {
        val preferences = context.dataStore.data.first()
        return preferences[FOLLOW_COUNT] ?: 0
    }

    suspend fun getRecipeCount(context: Context): Int {
        val preferences = context.dataStore.data.first()
        return preferences[RECIPE_COUNT] ?: 0
    }

    suspend fun getCreatedAt(context: Context): String {
        val preferences = context.dataStore.data.first()
        return preferences[CREATED_AT] ?: ""
    }

    suspend fun clearLoginState(context: Context) {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun setFollowCount(context: Context, followCount: Int) {
        context.dataStore.edit { preferences ->
            preferences[FOLLOW_COUNT] = followCount
        }
    }

    suspend fun setRecipeCount(context: Context, recipeCount: Int) {
        context.dataStore.edit { preferences ->
            preferences[RECIPE_COUNT] = recipeCount
        }
    }

    suspend fun updateLastShopping(context: Context, lastShoppingId: Int) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SHOPPING_ID] = lastShoppingId
            preferences[IS_FINISHED_SHOPPING] = false
        }
    }

    suspend fun isFinishedShopping(context: Context): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[IS_FINISHED_SHOPPING] ?: true
    }

    suspend fun getLastShoppingId(context: Context): Int {
        val preferences = context.dataStore.data.first()
        return preferences[LAST_SHOPPING_ID] ?: 0
    }

    suspend fun finishShopping(context: Context) {
        context.dataStore.edit { preferences ->
            preferences[IS_FINISHED_SHOPPING] = true
        }
    }
}