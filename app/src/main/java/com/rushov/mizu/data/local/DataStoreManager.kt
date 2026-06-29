package com.rushov.mizu.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mizu_prefs")

object DataStoreManager {
    private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    private val USER_ID_KEY = intPreferencesKey("user_id")
    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")

    suspend fun saveUserData(context: Context, token: String, userId: Int, name: String, email: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[USER_ID_KEY] = userId
            prefs[USER_NAME_KEY] = name
            prefs[USER_EMAIL_KEY] = email
        }
    }

    fun getToken(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs -> prefs[TOKEN_KEY] }
    }

    fun getUserId(context: Context): Flow<Int?> {
        return context.dataStore.data.map { prefs -> prefs[USER_ID_KEY] }
    }

    fun getUserName(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs -> prefs[USER_NAME_KEY] }
    }

    fun getUserEmail(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs -> prefs[USER_EMAIL_KEY] }
    }

    suspend fun clearUserData(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    fun isLoggedIn(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[TOKEN_KEY] != null
        }
    }
}
