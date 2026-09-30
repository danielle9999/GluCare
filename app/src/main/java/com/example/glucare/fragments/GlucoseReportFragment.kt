// GlucoseReportFragment.kt - Updated
package com.example.glucare.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.glucare.GlucareApplication
import com.example.glucare.R
import com.example.glucare.databinding.FragmentGlucoseReportBinding
import com.example.glucare.models.GlucoseReading
import com.example.glucare.utils.ChartUtils
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class GlucoseReportFragment : Fragment() {

    private var _binding: FragmentGlucoseReportBinding? = null
    private val binding get() = _binding!!
    private lateinit var app: GlucareApplication
    private val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
    private var currentPeriod = "7days" // 7days, 30days, 90days

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGlucoseReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = requireActivity().application as GlucareApplication

        setupPeriodSelector()
        setupCharts()
        loadGlucoseData()
    }

    private fun setupPeriodSelector() {
        binding.chip7days.setOnClickListener {
            currentPeriod = "7days"
            binding.chip7days.isChecked = true
            binding.chip30days.isChecked = false
            binding.chip90days.isChecked = false
            loadGlucoseData()
        }

        binding.chip30days.setOnClickListener {
            currentPeriod = "30days"
            binding.chip7days.isChecked = false
            binding.chip30days.isChecked = true
            binding.chip90days.isChecked = false
            loadGlucoseData()
        }

        binding.chip90days.setOnClickListener {
            currentPeriod = "90days"
            binding.chip7days.isChecked = false
            binding.chip30days.isChecked = false
            binding.chip90days.isChecked = true
            loadGlucoseData()
        }
    }

    private fun setupCharts() {
        ChartUtils.styleLineChart(binding.lineChart, requireContext())
    }

    private fun loadGlucoseData() {
        lifecycleScope.launch {
            val userId = app.sessionManager.getUserId()
            val days = when (currentPeriod) {
                "7days" -> 7
                "30days" -> 30
                "90days" -> 90
                else -> 7
            }

            val readings = withContext(Dispatchers.IO) {
                app.databaseHelper.getGlucoseReadings(userId)
                    .filter { it.timestamp >= getDaysAgo(days) }
                    .sortedBy { it.timestamp }
            }

            if (readings.isNotEmpty()) {
                updateLineChart(readings)
                updateStats(readings)
                updateTimeOfDayStats(readings)
                binding.tvNoData.visibility = View.GONE
            } else {
                binding.lineChart.clear()
                binding.tvNoData.visibility = View.VISIBLE
            }
        }
    }

    private fun updateLineChart(readings: List<GlucoseReading>) {
        val entries = readings.mapIndexed { index, reading ->
            Entry(index.toFloat(), reading.value.toFloat())
        }

        val dataSet = ChartUtils.createGlucoseDataSet(entries, "Glucose (mg/dL)")
        val lineData = LineData(dataSet)

        val xAxis = binding.lineChart.xAxis
        xAxis.valueFormatter = object : IndexAxisValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index in readings.indices) {
                    dateFormat.format(Date(readings[index].timestamp * 1000))
                } else ""
            }
        }

        binding.lineChart.data = lineData
        binding.lineChart.invalidate()
    }

    private fun updateStats(readings: List<GlucoseReading>) {
        val values = readings.map { it.value }
        val avg = values.average().toInt()
        val min = values.minOrNull() ?: 0
        val max = values.maxOrNull() ?: 0

        val low = values.count { it < 70 }
        val normal = values.count { it in 70..140 }
        val elevated = values.count { it in 141..180 }
        val high = values.count { it > 180 }
        val total = values.size.toFloat()

        binding.tvAvgGlucose.text = "$avg mg/dL"
        binding.tvMinGlucose.text = "$min"
        binding.tvMaxGlucose.text = "$max"

        binding.tvLowPercent.text = String.format("%.1f%%", (low / total * 100))
        binding.tvNormalPercent.text = String.format("%.1f%%", (normal / total * 100))
        binding.tvElevatedPercent.text = String.format("%.1f%%", (elevated / total * 100))
        binding.tvHighPercent.text = String.format("%.1f%%", (high / total * 100))

        // Calculate estimated A1C (average glucose to A1C conversion)
        // Formula: Estimated A1C (%) = (Average Glucose + 46.7) / 28.7
        val estimatedA1C = (avg.toFloat() + 46.7f) / 28.7f
        binding.tvEstimatedA1C.text = String.format("%.1f%%", estimatedA1C)
    }

    private fun updateTimeOfDayStats(readings: List<GlucoseReading>) {
        fun Long.toHour(): Int {
            val cal = Calendar.getInstance()
            cal.timeInMillis = this * 1000
            return cal.get(Calendar.HOUR_OF_DAY)
        }

        // FIX: use takeIf to avoid NaN from average() on empty lists
        fun List<GlucoseReading>.avgForHours(vararg ranges: IntRange): String {
            val vals = filter { r -> ranges.any { r.timestamp.toHour() in it } }.map { it.value }
            return if (vals.isEmpty()) "--" else "${vals.average().toInt()} mg/dL"
        }

        binding.tvMorningAvg.text   = readings.avgForHours(5..11)
        binding.tvAfternoonAvg.text = readings.avgForHours(12..16)
        binding.tvEveningAvg.text   = readings.avgForHours(17..20)
        binding.tvNightAvg.text     = readings.avgForHours(21..23, 0..4)
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