package com.rushov.mizu.presentation.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import com.rushov.mizu.data.remote.TransactionResponse
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PDFHelper {
    fun exportTransactionsToPDF(
        context: Context,
        transactions: List<TransactionResponse>,
        userName: String,
        onResult: (String) -> Unit
    ) {
        try {
            if (transactions.isEmpty()) {
                onResult("No transactions to export")
                return
            }

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()

            paint.textSize = 24f
            paint.isFakeBoldText = true
            canvas.drawText("MIZU - Transaction Report", 50f, 50f, paint)

            paint.textSize = 14f
            paint.isFakeBoldText = false
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            canvas.drawText("Generated: " + dateFormat.format(Date()), 50f, 80f, paint)
            canvas.drawText("User: " + userName, 50f, 100f, paint)

            paint.textSize = 12f
            paint.isFakeBoldText = true
            var y = 140f
            canvas.drawText("Date", 50f, y, paint)
            canvas.drawText("Title", 150f, y, paint)
            canvas.drawText("Category", 280f, y, paint)
            canvas.drawText("Type", 380f, y, paint)
            canvas.drawText("Amount", 450f, y, paint)

            y += 10f
            canvas.drawLine(50f, y, 545f, y, paint)

            paint.isFakeBoldText = false
            y += 25f
            transactions.forEach { t ->
                paint.textSize = 10f
                canvas.drawText(t.created_at.take(10), 50f, y, paint)
                canvas.drawText(t.title.take(20), 150f, y, paint)
                canvas.drawText(t.category.take(15), 280f, y, paint)
                canvas.drawText(t.type, 380f, y, paint)
                val sign = if (t.type == "income") "+" else "-"
                canvas.drawText(sign + String.format("%.2f", t.amount), 450f, y, paint)
                y += 20f
            }

            y += 30f
            paint.textSize = 14f
            paint.isFakeBoldText = true
            val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
            val totalExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
            val balance = totalIncome - totalExpense
            canvas.drawText("Summary:", 50f, y, paint)
            y += 25f
            paint.isFakeBoldText = false
            canvas.drawText("Total Income: +" + String.format("%.2f", totalIncome), 50f, y, paint)
            y += 20f
            canvas.drawText("Total Expense: -" + String.format("%.2f", totalExpense), 50f, y, paint)
            y += 20f
            paint.isFakeBoldText = true
            canvas.drawText("Balance: " + String.format("%.2f", balance), 50f, y, paint)

            pdfDocument.finishPage(page)

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val fileName = "MIZU_Report_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date()) + ".pdf"
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { output ->
                pdfDocument.writeTo(output)
            }
            pdfDocument.close()

            val uri = FileProvider.getUriForFile(
                context,
                context.packageName + ".provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "MIZU Transaction Report")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share PDF"))
            onResult("PDF exported to Downloads/" + fileName)

        } catch (e: Exception) {
            onResult("Error: " + e.message)
        }
    }
}
