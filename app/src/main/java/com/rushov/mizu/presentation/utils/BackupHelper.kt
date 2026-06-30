package com.rushov.mizu.presentation.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rushov.mizu.data.remote.RetrofitClient
import com.rushov.mizu.data.remote.TransactionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object BackupHelper {
    data class BackupData(
        val transactions: List<TransactionResponse>,
        val backupDate: String
    )

    suspend fun exportToJSON(context: Context, userId: Int): String = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.api.getTransactions(userId)
            if (!response.isSuccessful || response.body() == null) {
                return@withContext "Failed to fetch data"
            }

            val transactions = response.body()!!
            val backupData = BackupData(
                transactions = transactions,
                backupDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )

            val gson = Gson()
            val jsonString = gson.toJson(backupData)

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val fileName = "MIZU_Backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
            val file = File(downloadsDir, fileName)

            FileWriter(file).use { writer ->
                writer.write(jsonString)
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "MIZU Backup")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Share Backup"))
            "Backup saved to Downloads/$fileName"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    suspend fun importFromJSON(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val jsonString = inputStream?.bufferedReader()?.use { it.readText() } ?: return@withContext "Failed to read file"

            val gson = Gson()
            val type = object : TypeToken<BackupData>() {}.type
            val backupData = gson.fromJson<BackupData>(jsonString, type)

            "Import successful! ${backupData.transactions.size} transactions found from ${backupData.backupDate}"
        } catch (e: Exception) {
            "Error importing: ${e.message}"
        }
    }
}
