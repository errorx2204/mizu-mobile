package com.rushov.mizu.presentation.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.currencyDataStore by preferencesDataStore(name = "currency_prefs")

object CurrencyHelper {
    private val CURRENCY_KEY = stringPreferencesKey("selected_currency")

    val currencies = mapOf(
        "INR" to "Rs.",
        "USD" to "$",
        "EUR" to "EUR",
        "GBP" to "GBP",
        "JPY" to "JPY"
    )

    fun getSelectedCurrency(context: Context): Flow<String> {
        return context.currencyDataStore.data.map { prefs ->
            prefs[CURRENCY_KEY] ?: "INR"
        }
    }

    suspend fun setCurrency(context: Context, currency: String) {
        context.currencyDataStore.edit { prefs ->
            prefs[CURRENCY_KEY] = currency
        }
    }

    fun getSymbol(currency: String): String {
        return currencies[currency] ?: "Rs."
    }
}
