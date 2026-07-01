package com.rushov.mizu.data.local

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class RecurringTransactionData(
    val id: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val type: String,
    val frequency: String,
    val nextDate: String
) {
    companion object {
        fun fromJson(json: String): RecurringTransactionData {
            return Json.decodeFromString(json)
        }
        
        fun toJson(transaction: RecurringTransactionData): String {
            return Json.encodeToString(transaction)
        }
        
        fun listFromJson(json: String): List<RecurringTransactionData> {
            return if (json.isBlank()) emptyList() else Json.decodeFromString(json)
        }
        
        fun listToJson(transactions: List<RecurringTransactionData>): String {
            return Json.encodeToString(transactions)
        }
    }
}
