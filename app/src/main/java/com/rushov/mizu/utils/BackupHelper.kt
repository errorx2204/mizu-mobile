package com.rushov.mizu.utils

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.rushov.mizu.data.local.DataStoreManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.FileWriter
import java.io.InputStreamReader

object BackupHelper {
    fun exportData(context: Context, uri: Uri) {
        val dataStore = DataStoreManager(context)
        val data = runBlocking {
            mapOf(
                "user_id" to dataStore.userId.first(),
                "user_name" to dataStore.userName.first(),
                "user_email" to dataStore.userEmail.first()
            )
        }
        val json = Gson().toJson(data)
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(json.toByteArray())
        }
    }

    fun importData(context: Context, uri: Uri) {
        val json = context.contentResolver.openInputStream(uri)?.use { stream ->
            InputStreamReader(stream).readText()
        } ?: return
        // Parse and restore data
    }
}
