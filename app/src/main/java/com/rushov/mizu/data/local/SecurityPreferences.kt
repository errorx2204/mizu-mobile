package com.rushov.mizu.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.securityDataStore by preferencesDataStore("security_prefs")

class SecurityPreferences(private val context: Context) {

    companion object {
        private val PIN_KEY = stringPreferencesKey("app_pin")
        private val BIOMETRIC_KEY =
            booleanPreferencesKey("biometric_enabled")
    }

    val savedPin: Flow<String> =
        context.securityDataStore.data.map {
            it[PIN_KEY] ?: "1234"
        }

    val biometricEnabled: Flow<Boolean> =
        context.securityDataStore.data.map {
            it[BIOMETRIC_KEY] ?: false
        }

    suspend fun savePin(pin: String) {
        context.securityDataStore.edit {
            it[PIN_KEY] = pin
        }
    }

    suspend fun setBiometric(enabled: Boolean) {
        context.securityDataStore.edit {
            it[BIOMETRIC_KEY] = enabled
        }
    }
}