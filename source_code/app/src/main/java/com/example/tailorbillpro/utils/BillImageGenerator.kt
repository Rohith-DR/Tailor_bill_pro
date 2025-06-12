package com.example.tailorbillpro.utils

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.tailorbillpro.data.entity.BillEntity
import com.example.tailorbillpro.data.entity.BillItemEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object BillImageGenerator {
    fun generateBillImage(
        context: Context,
        bill: BillEntity,
        items: List<BillItemEntity>,
        clientName: String
    ): String {
        val width = 800
        val height = 1200
        val padding = 40f
        val lineSpacing = 35f

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 30f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val titlePaint = Paint(paint).apply {
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
        }

        val headerPaint = Paint(paint).apply {
            textSize = 35f
            typeface = Typeface.DEFAULT_BOLD
        }

        var y = padding + 50f

        // Draw Title centered
        val titleText = "TailorBillPro"
        val titleWidth = titlePaint.measureText(titleText)
        canvas.drawText(titleText, (width - titleWidth) / 2, y, titlePaint)
        y += lineSpacing * 2

        // Draw Date
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        canvas.drawText("Date: ${dateFormat.format(bill.date)}", padding, y, paint)
        y += lineSpacing

        // Draw Client Name
        canvas.drawText("Client: $clientName", padding, y, paint)
        y += lineSpacing * 2

        // Draw Items Header centered
        val itemsHeaderText = "Items"
        val itemsHeaderWidth = headerPaint.measureText(itemsHeaderText)
        canvas.drawText(itemsHeaderText, (width - itemsHeaderWidth) / 2, y, headerPaint)
        y += lineSpacing
        canvas.drawLine(padding, y, width - padding, y, paint)
        y += lineSpacing

        // Draw Items
        items.forEach { item ->
            val itemText = "${item.serviceName} (${item.quantity} x ₹${item.price})"
            canvas.drawText(itemText, padding, y, paint)
            val amount = item.quantity * item.price
            val amountText = "₹$amount"
            canvas.drawText(
                amountText,
                width - padding - paint.measureText(amountText),
                y,
                paint
            )
            y += lineSpacing
        }

        y += lineSpacing
        canvas.drawLine(padding, y, width - padding, y, paint)
        y += lineSpacing * 1.5f

        // Draw Total centered
        val totalText = "Total Amount: ₹${bill.totalAmount}"
        val totalWidth = headerPaint.measureText(totalText)
        canvas.drawText(
            totalText,
            (width - totalWidth) / 2,
            y,
            headerPaint
        )

        // Save bitmap to file
        val billsDir = File(context.getExternalFilesDir(null), "bills").apply {
            if (!exists()) mkdirs()
        }
        val imageFile = File(billsDir, "bill_${bill.id}.png")
        imageFile.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        return imageFile.absolutePath
    }

    fun getBillUri(context: Context, billId: Long): Uri {
        val billsDir = File(context.getExternalFilesDir(null), "bills")
        val file = File(billsDir, "bill_$billId.png")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
}
