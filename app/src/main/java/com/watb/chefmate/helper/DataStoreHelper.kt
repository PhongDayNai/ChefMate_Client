package com.watb.chefmate.helper

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
val USER_ID = intPreferencesKey("user_id")
val USERNAME = stringPreferencesKey("username")
val EMAIL = stringPreferencesKey("email")
val PHONE_NUMBER = stringPreferencesKey("phone_number")
val FOLLOW_COUNT = intPreferencesKey("follow_count")
val RECIPE_COUNT = intPreferencesKey("recipe_count")
val CREATED_AT = stringPreferencesKey("created_at")

object DataStoreHelper {
}