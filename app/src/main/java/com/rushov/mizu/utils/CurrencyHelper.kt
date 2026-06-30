package com.rushov.mizu.utils

object CurrencyHelper {
    private val exchangeRates = mapOf(
        "INR" to 1.0,
        "USD" to 0.012,
        "EUR" to 0.011,
        "GBP" to 0.0095
    )

    fun convert(amount: Double, from: String, to: String): Double {
        val fromRate = exchangeRates[from] ?: 1.0
        val toRate = exchangeRates[to] ?: 1.0
        return amount * (toRate / fromRate)
    }

    fun getSymbol(currency: String): String = when (currency) {
        "INR" -> "Rs."
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        else -> "Rs."
    }
}
