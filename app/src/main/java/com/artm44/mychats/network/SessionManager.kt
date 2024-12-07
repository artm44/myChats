package com.artm44.mychats.network

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionManager(context: Context) {

    private val Context.dataStore by preferencesDataStore("user_prefs")
    private val dataStore = context.dataStore

    private val TOKEN_KEY = stringPreferencesKey("auth_token")
    private val USERNAME_KEY = stringPreferencesKey("auth_username")

    val token: Flow<String?> = dataStore.data.map { it[TOKEN_KEY] }

    val username: Flow<String?> = dataStore.data.map { it[USERNAME_KEY] }

    suspend fun saveSession(token: String, username: String) {
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USERNAME_KEY] = username
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(USERNAME_KEY)
        }
    }
}
