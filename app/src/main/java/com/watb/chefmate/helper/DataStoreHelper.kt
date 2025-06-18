package com.watb.chefmate.helper

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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