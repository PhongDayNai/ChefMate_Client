package com.watb.chefmate.helper

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.watb.chefmate.data.MealRecipeState
import com.watb.chefmate.data.MealSessionUiState
import com.watb.chefmate.data.PersistedMealSnapshot
import kotlinx.coroutines.flow.first

private val Context.chatSnapshotStore by preferencesDataStore(name = "chatSnapshotStore")

object ChatSnapshotStore {
    private val gson = Gson()
    private lateinit var appContext: Context

    fun init(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext
    }

    suspend fun saveLastChatSessionId(userId: Int, sessionId: Int?) {
        ensureInitialized()
        val key = intPreferencesKey("last_chat_session_$userId")
        appContext.chatSnapshotStore.edit { preferences ->
            if (sessionId == null || sessionId <= 0) {
                preferences.remove(key)
            } else {
                preferences[key] = sessionId
            }
        }
    }

    suspend fun getLastChatSessionId(userId: Int): Int? {
        ensureInitialized()
        val preferences = appContext.chatSnapshotStore.data.first()
        return preferences[intPreferencesKey("last_chat_session_$userId")]
    }

    suspend fun saveMealSnapshot(
        userId: Int,
        sessionId: Int,
        mealSession: MealSessionUiState,
        mealItems: List<MealRecipeState>
    ) {
        ensureInitialized()
        if (sessionId <= 0) return
        val key = stringPreferencesKey("meal_snapshot_${userId}_$sessionId")
        val payload = PersistedMealSnapshot(
            mealSession = mealSession,
            mealItems = mealItems
        )
        appContext.chatSnapshotStore.edit { preferences ->
            preferences[key] = gson.toJson(payload)
        }
    }

    suspend fun getMealSnapshot(userId: Int, sessionId: Int): PersistedMealSnapshot? {
        ensureInitialized()
        if (sessionId <= 0) return null
        val preferences = appContext.chatSnapshotStore.data.first()
        val raw = preferences[stringPreferencesKey("meal_snapshot_${userId}_$sessionId")] ?: return null
        return runCatching {
            gson.fromJson(raw, PersistedMealSnapshot::class.java)
        }.getOrNull()
    }

    suspend fun clearUser(userId: Int) {
        ensureInitialized()
        appContext.chatSnapshotStore.edit { preferences ->
            val keysToRemove = preferences.asMap().keys.map { it.name }.filter { name ->
                name == "last_chat_session_$userId" || name.startsWith("meal_snapshot_${userId}_")
            }
            keysToRemove.forEach { name ->
                when {
                    name == "last_chat_session_$userId" -> preferences.remove(intPreferencesKey(name))
                    name.startsWith("meal_snapshot_${userId}_") -> preferences.remove(stringPreferencesKey(name))
                }
            }
        }
    }

    suspend fun clearAll() {
        ensureInitialized()
        appContext.chatSnapshotStore.edit { preferences ->
            preferences.clear()
        }
    }

    private fun ensureInitialized() {
        check(::appContext.isInitialized) {
            "ChatSnapshotStore.init(context) must be called before use"
        }
    }
}
