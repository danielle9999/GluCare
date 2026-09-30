package com.example.glucare.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.glucare.R
import com.example.glucare.models.GlucoseReading
import com.example.glucare.models.InsulinDose
import com.example.glucare.models.CalendarEvent
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PDFGenerator(private val context: Context) {

    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    fun generateReport(
        userName: String,
        glucoseReadings: List<GlucoseReading>,
        insulinDoses: List<InsulinDose>,
        events: List<CalendarEvent>,
        startDate: Date,
        endDate: Date
    ): File {

        // Create a new PDF document
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint().apply {
            color = Color.parseColor("#2196F3") // Primary blue
            textSize = 24f
            isFakeBoldText = true
        }
        val headerPaint = Paint().apply {
            color = Color.parseColor("#333333")
            textSize = 16f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            color = Color.parseColor("#666666")
            textSize = 12f
        }
        val smallPaint = Paint().apply {
            color = Color.parseColor("#999999")
            textSize = 10f
        }

        // Page info
        val pageWidth = 612 // Letter size in points
        val pageHeight = 792
        var yPosition = 50f
        val margin = 50f
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Helper function to check if we need a new page
        fun checkNewPage(): Boolean {
            if (yPosition > pageHeight - 100) {
                pdfDocument.finishPage(page)
                val newPage = pdfDocument.startPage(pageInfo)
                canvas = newPage.canvas
                page = newPage
                yPosition = 50f
                return true
            }
            return false
        }

        // Title
        canvas.drawText("GLUCARE HEALTH REPORT", margin, yPosition, titlePaint)
        yPosition += 40f

        // Date range
        val dateRange = "Report Period: ${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
        canvas.drawText(dateRange, margin, yPosition, smallPaint)
        yPosition += 30f

        // Patient name
        canvas.drawText("Patient: $userName", margin, yPosition, textPaint)
        yPosition += 30f

        // Generated date
        val generatedDate = "Generated: ${dateFormat.format(Date())}"
        canvas.drawText(generatedDate, margin, yPosition, smallPaint)
        yPosition += 40f

        // Draw line
        paint.color = Color.parseColor("#DDDDDD")
        paint.strokeWidth = 2f
        canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, paint)
        yPosition += 30f

        // Glucose Summary Section
        canvas.drawText("GLUCOSE SUMMARY", margin, yPosition, headerPaint)
        yPosition += 25f

        if (glucoseReadings.isNotEmpty()) {
            val avgGlucose = glucoseReadings.map { it.value }.average().toInt()
            val minGlucose = glucoseReadings.minByOrNull { it.value }?.value ?: 0
            val maxGlucose = glucoseReadings.maxByOrNull { it.value }?.value ?: 0

            // Time in range calculations
            val total = glucoseReadings.size
            val low = glucoseReadings.count { it.value < 70 }
            val normal = glucoseReadings.count { it.value in 70..140 }
            val elevated = glucoseReadings.count { it.value in 141..180 }
            val high = glucoseReadings.count { it.value > 180 }

            canvas.drawText("Average Glucose: $avgGlucose mg/dL", margin + 20, yPosition, textPaint)
            yPosition += 20f
            canvas.drawText("Range: $minGlucose - $maxGlucose mg/dL", margin + 20, yPosition, textPaint)
            yPosition += 20f
            canvas.drawText("Total Readings: ${glucoseReadings.size}", margin + 20, yPosition, textPaint)
            yPosition += 25f

            canvas.drawText("Time in Range:", margin + 20, yPosition, textPaint)
            yPosition += 20f
            canvas.drawText("• Low (<70): ${String.format("%.1f%%", (low.toFloat() / total * 100))}", margin + 40, yPosition, textPaint)
            yPosition += 20f
            canvas.drawText("• Normal (70-140): ${String.format("%.1f%%", (normal.toFloat() / total * 100))}", margin + 40, yPosition, textPaint)
            yPosition += 20f
            canvas.drawText("• Elevated (141-180): ${String.format("%.1f%%", (elevated.toFloat() / total * 100))}", margin + 40, yPosition, textPaint)
            yPosition += 20f
            canvas.drawText("• High (>180): ${String.format("%.1f%%", (high.toFloat() / total * 100))}", margin + 40, yPosition, textPaint)
        } else {
            canvas.drawText("No glucose data available", margin + 20, yPosition, textPaint)
        }
        yPosition += 30f

        checkNewPage()

        // Insulin Summary Section
        canvas.drawText("INSULIN SUMMARY", margin, yPosition, headerPaint)
        yPosition += 25f

        if (insulinDoses.isNotEmpty()) {
            val totalInsulin = insulinDoses.sumOf { it.calculatedDose.toDouble() }.toInt()
            val avgDose = insulinDoses.map { it.calculatedDose }.average()
            val totalCarbs = insulinDoses.sumOf { it.carbs.toDouble() }.toInt()

            canvas.drawText("Total Insulin: $totalInsulin units", margin + 20, yPosition, textPaint)
            yPosition += 20f
            canvas.drawText("Average Dose: ${String.format("%.1f", avgDose)} units", margin + 20, yPosition, textPaint)
            yPosition += 20f
            canvas.drawText("Total Carbs: $totalCarbs g", margin + 20, yPosition, textPaint)
            yPosition += 20f
            canvas.drawText("Total Doses: ${insulinDoses.size}", margin + 20, yPosition, textPaint)

            // Calculate insulin-to-carb ratio
            if (totalInsulin > 0) {
                val ratio = totalCarbs / totalInsulin
                yPosition += 20f
                canvas.drawText("Avg I:C Ratio: 1 unit : $ratio g carbs", margin + 20, yPosition, textPaint)
            }
        } else {
            canvas.drawText("No insulin data available", margin + 20, yPosition, textPaint)
        }
        yPosition += 30f

        checkNewPage()

        // Events Summary Section
        canvas.drawText("UPCOMING EVENTS", margin, yPosition, headerPaint)
        yPosition += 25f

        if (events.isNotEmpty()) {
            events.take(10).forEach { event ->
                val eventDate = dateFormat.format(Date(event.date))
                val eventTime = event.time ?: "All day"
                canvas.drawText("• ${event.title} - $eventDate $eventTime", margin + 20, yPosition, textPaint)
                yPosition += 20f

                if (checkNewPage()) {
                    canvas.drawText("UPCOMING EVENTS (continued)", margin, yPosition, headerPaint)
                    yPosition += 25f
                }
            }
        } else {
            canvas.drawText("No upcoming events", margin + 20, yPosition, textPaint)
        }
        yPosition += 30f

        // Footer
        checkNewPage()
        paint.color = Color.parseColor("#DDDDDD")
        canvas.drawLine(margin, yPosition, pageWidth - margin, yPosition, paint)
        yPosition += 20f
        canvas.drawText("This report was generated by Glucare Diabetes Management App", margin, yPosition, smallPaint)
        yPosition += 15f
        canvas.drawText("For medical advice, please consult your healthcare provider.", margin, yPosition, smallPaint)

        // Finish the last page
        pdfDocument.finishPage(page)

        // Save the document
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val fileName = "Glucare_Report_${System.currentTimeMillis()}.pdf"
        val file = File(downloadsDir, fileName)

        try {
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
        } finally {
            pdfDocument.close()
        }

        return file
    }
}