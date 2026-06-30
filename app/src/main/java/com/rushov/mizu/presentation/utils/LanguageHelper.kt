package com.rushov.mizu.presentation.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.languageDataStore by preferencesDataStore(name = "language_settings")

object LanguageHelper {
    private val LANGUAGE_KEY = stringPreferencesKey("selected_language")
    
    val supportedLanguages = mapOf(
        "en" to "English",
        "hi" to "Hindi",
        "bn" to "Bengali"
    )
    
    fun getSelectedLanguage(context: Context): Flow<String> {
        return context.languageDataStore.data.map { preferences ->
            preferences[LANGUAGE_KEY] ?: "en"
        }
    }
    
    suspend fun setLanguage(context: Context, languageCode: String) {
        context.languageDataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }
    
    fun applyLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}
