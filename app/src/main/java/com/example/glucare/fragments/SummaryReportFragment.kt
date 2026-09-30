package com.example.glucare.fragments

import android.content.Intent
import com.example.glucare.utils.PDFGenerator
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.glucare.GlucareApplication
import com.example.glucare.R
import com.example.glucare.databinding.FragmentSummaryReportBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.*

class SummaryReportFragment : Fragment() {

    private var _binding: FragmentSummaryReportBinding? = null
    private val binding get() = _binding!!
    private lateinit var app: GlucareApplication
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = requireActivity().application as GlucareApplication

        loadSummaryData()
        setupButtons()
    }

    private fun loadSummaryData() {
        lifecycleScope.launch {
            val userId = app.sessionManager.getUserId()
            val days = 30

            // Get all data
            val glucoseReadings = withContext(Dispatchers.IO) {
                app.databaseHelper.getGlucoseReadings(userId)
                    .filter { it.timestamp >= getDaysAgo(days) }
            }

            val insulinDoses = withContext(Dispatchers.IO) {
                app.databaseHelper.getInsulinHistory(userId)
                    .filter { it.timestamp >= getDaysAgo(days) }
            }

            val events = withContext(Dispatchers.IO) {
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                app.databaseHelper.getEventsForDay(userId, todayStart)
            }

            updateSummary(glucoseReadings, insulinDoses, events)
        }
    }

    private fun updateSummary(
        glucoseReadings: List<com.example.glucare.models.GlucoseReading>,
        insulinDoses: List<com.example.glucare.models.InsulinDose>,
        events: List<com.example.glucare.models.CalendarEvent>
    ) {
        // Glucose stats
        if (glucoseReadings.isNotEmpty()) {
            val avgGlucose = glucoseReadings.map { it.value }.average().toInt()
            val lastReading = glucoseReadings.maxByOrNull { it.timestamp }

            binding.tvAvgGlucose30.text = "$avgGlucose mg/dL"
            binding.tvLastReading.text = lastReading?.value?.toString() ?: "--"
            binding.tvLastReadingDate.text = lastReading?.let {
                dateFormat.format(Date(it.timestamp * 1000))
            } ?: "--"
        } else {
            binding.tvAvgGlucose30.text = "--"
            binding.tvLastReading.text = "--"
            binding.tvLastReadingDate.text = "--"
        }

        // Insulin stats
        if (insulinDoses.isNotEmpty()) {
            val totalInsulin = insulinDoses.sumOf { it.calculatedDose.toDouble() }.toInt()
            val avgDaily = totalInsulin / 30

            binding.tvTotalInsulin30.text = "$totalInsulin units"
            binding.tvAvgDailyInsulin.text = "$avgDaily units/day"
        } else {
            binding.tvTotalInsulin30.text = "--"
            binding.tvAvgDailyInsulin.text = "--"
        }

        // Event stats
        binding.tvTodayEvents.text = events.size.toString()

        // Most common time of day for readings
        if (glucoseReadings.isNotEmpty()) {
            val timeStats = glucoseReadings.groupBy { reading ->
                val hour = Calendar.getInstance().apply {
                    timeInMillis = reading.timestamp * 1000
                }.get(Calendar.HOUR_OF_DAY)

                when (hour) {
                    in 5..11 -> "Morning"
                    in 12..16 -> "Afternoon"
                    in 17..20 -> "Evening"
                    else -> "Night"
                }
            }.mapValues { it.value.size }

            val mostCommonTime = timeStats.maxByOrNull { it.value }?.key ?: "N/A"
            binding.tvMostCommonTime.text = mostCommonTime
        } else {
            binding.tvMostCommonTime.text = "N/A"
        }
    }

    private fun setupButtons() {
        binding.btnShareReport.setOnClickListener {
            generateAndShareReport()
        }

        binding.btnExportCSV.setOnClickListener {
            exportToCSV()
        }
        binding.btnExportPDF.setOnClickListener {
            exportToPDF()
        }
    }

    private fun generateAndShareReport() {
        lifecycleScope.launch {
            val report = withContext(Dispatchers.IO) {
                generateReportText()
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Glucare Health Report")
                putExtra(Intent.EXTRA_TEXT, report)
            }
            startActivity(Intent.createChooser(intent, "Share Report"))
        }
    }

    private fun generateReportText(): String {
        val userId = app.sessionManager.getUserId()
        val userName = app.sessionManager.getUserName() ?: "User"
        val date = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date())

        val glucoseReadings = app.databaseHelper.getGlucoseReadings(userId)
            .filter { it.timestamp >= getDaysAgo(30) }
        val insulinDoses = app.databaseHelper.getInsulinHistory(userId)
            .filter { it.timestamp >= getDaysAgo(30) }

        val avgGlucose = if (glucoseReadings.isNotEmpty()) {
            glucoseReadings.map { it.value }.average().toInt()
        } else 0

        val totalInsulin = if (insulinDoses.isNotEmpty()) {
            insulinDoses.sumOf { it.calculatedDose.toDouble() }.toInt()
        } else 0

        return """
            GLUCARE HEALTH REPORT
            ====================
            
            Patient: $userName
            Date: $date
            
            📊 30-DAY SUMMARY
            -----------------
            Average Glucose: $avgGlucose mg/dL
            Total Insulin: $totalInsulin units
            Readings Logged: ${glucoseReadings.size}
            Insulin Doses: ${insulinDoses.size}
            
            💉 INSULIN BREAKDOWN
            -------------------
            Daily Average: ${if (insulinDoses.isNotEmpty()) totalInsulin / 30 else 0} units/day
            
            📈 GLUCOSE STATS
            ----------------
            ${generateGlucoseStats(glucoseReadings)}
            
            This report was generated by Glucare Diabetes Management App.
            For medical advice, please consult your healthcare provider.
        """.trimIndent()
    }

    private fun generateGlucoseStats(readings: List<com.example.glucare.models.GlucoseReading>): String {
        if (readings.isEmpty()) return "No glucose data available"

        val low = readings.count { it.value < 70 }
        val normal = readings.count { it.value in 70..140 }
        val elevated = readings.count { it.value in 141..180 }
        val high = readings.count { it.value > 180 }
        val total = readings.size.toFloat()

        return """
            Time in Range:
            • Low (<70): ${String.format("%.1f%%", if (total > 0) (low / total * 100) else 0f)}
            • Normal (70-140): ${String.format("%.1f%%", if (total > 0) (normal / total * 100) else 0f)}
            • Elevated (141-180): ${String.format("%.1f%%", if (total > 0) (elevated / total * 100) else 0f)}
            • High (>180): ${String.format("%.1f%%", if (total > 0) (high / total * 100) else 0f)}
        """.trimIndent()
    }

    private fun exportToCSV() {
        lifecycleScope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    createCSVFile()
                }

                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(Intent.createChooser(intent, "Export CSV"))

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error exporting CSV: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createCSVFile(): File {
        val userId = app.sessionManager.getUserId()
        val downloadsDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, "glucare_export_${System.currentTimeMillis()}.csv")

        PrintWriter(FileWriter(file)).use { writer ->
            // Write glucose readings
            writer.println("GLUCOSE READINGS")
            writer.println("Date,Time,Value (mg/dL),Type,Notes")

            val readings = app.databaseHelper.getGlucoseReadings(userId)
                .sortedByDescending { it.timestamp }
                .take(100)

            val sdf = SimpleDateFormat("yyyy-MM-dd,HH:mm", Locale.getDefault())
            readings.forEach { reading ->
                val dateStr = sdf.format(Date(reading.timestamp * 1000))
                writer.println("$dateStr,${reading.value},${reading.readingType},${reading.notes ?: ""}")
            }

            writer.println()

            // Write insulin doses
            writer.println("INSULIN DOSES")
            writer.println("Date,Time,Glucose,Carbs (g),Dose (units)")

            val doses = app.databaseHelper.getInsulinHistory(userId)
                .sortedByDescending { it.timestamp }
                .take(100)

            doses.forEach { dose ->
                val dateStr = sdf.format(Date(dose.timestamp * 1000))
                writer.println("$dateStr,${dose.glucoseValue ?: ""},${dose.carbs},${dose.calculatedDose}")
            }
        }

        return file
    }
    private fun exportToPDF() {
        lifecycleScope.launch {
            try {
                // Show loading
                Toast.makeText(requireContext(), "Generating PDF...", Toast.LENGTH_SHORT).show()

                val file = withContext(Dispatchers.IO) {
                    generatePDF()
                }

                // Open the PDF
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(Intent.createChooser(intent, "Open PDF"))

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun generatePDF(): File {
        val userId = app.sessionManager.getUserId()
        val userName = app.sessionManager.getUserName() ?: "User"

        // Get data for last 30 days
        val endDate = Date()
        val cal = Calendar.getInstance().apply {
            time = endDate
            add(Calendar.DAY_OF_YEAR, -30)
        }
        val startDate = cal.time
        val startTimestamp = startDate.time / 1000

        val glucoseReadings = app.databaseHelper.getGlucoseReadings(userId)
            .filter { it.timestamp >= startTimestamp }

        val insulinDoses = app.databaseHelper.getInsulinHistory(userId)
            .filter { it.timestamp >= startTimestamp }

        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val events = app.databaseHelper.getEventsForDay(userId, todayStart)

        val pdfGenerator = PDFGenerator(requireContext())
        return pdfGenerator.generateReport(
            userName = userName,
            glucoseReadings = glucoseReadings,
            insulinDoses = insulinDoses,
            events = events,
            startDate = startDate,
            endDate = endDate
        )
    }

    private fun getDaysAgo(days: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return cal.timeInMillis / 1000
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}