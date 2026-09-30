package com.example.glucare.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.glucare.GlucareApplication
import com.example.glucare.R
import com.example.glucare.databinding.FragmentInsulinReportBinding
import com.example.glucare.models.InsulinDose
import com.example.glucare.utils.ChartUtils
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class InsulinReportFragment : Fragment() {

    private var _binding: FragmentInsulinReportBinding? = null
    private val binding get() = _binding!!
    private lateinit var app: GlucareApplication
    private val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInsulinReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        app = requireActivity().application as GlucareApplication

        setupCharts()
        loadInsulinData()
    }

    private fun setupCharts() {
        ChartUtils.styleBarChart(binding.barChartDaily, requireContext())
        ChartUtils.stylePieChart(binding.pieChartEffectiveness, requireContext())
    }

    private fun loadInsulinData() {
        lifecycleScope.launch {
            val userId = app.sessionManager.getUserId()
            val days = 7

            val doses = withContext(Dispatchers.IO) {
                app.databaseHelper.getInsulinHistory(userId)
                    .filter { it.timestamp >= getDaysAgo(days) }
                    .sortedBy { it.timestamp }
            }

            if (doses.isNotEmpty()) {
                updateDailyBarChart(doses)
                updateEffectivenessPieChart(doses)
                updateStats(doses)
                binding.tvNoData.visibility = View.GONE
            } else {
                binding.barChartDaily.clear()
                binding.pieChartEffectiveness.clear()
                binding.tvNoData.visibility = View.VISIBLE
            }
        }
    }

    private fun updateDailyBarChart(doses: List<InsulinDose>) {
        // Group doses by day
        val dosesByDay = doses.groupBy { dose ->
            val date = Date(dose.timestamp * 1000)
            dateFormat.format(date)
        }

        // Create entries for each day
        val entries = dosesByDay.values.mapIndexed { index, dayDoses ->
            val totalDose = dayDoses.sumOf { it.calculatedDose.toDouble() }.toFloat()
            BarEntry(index.toFloat(), totalDose)
        }

        val dataSet = ChartUtils.createInsulinDataSet(entries, "Daily Insulin (units)")
        val barData = BarData(dataSet)
        barData.barWidth = 0.9f

        // Set x-axis labels
        val xAxis = binding.barChartDaily.xAxis
        xAxis.valueFormatter = object : IndexAxisValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index < dosesByDay.keys.size) {
                    dosesByDay.keys.toList()[index]
                } else ""
            }
        }

        binding.barChartDaily.data = barData
        binding.barChartDaily.invalidate()
    }

    private fun updateEffectivenessPieChart(doses: List<InsulinDose>) {
        // Calculate effectiveness scores
        val effective = doses.count { dose ->
            dose.effectiveness != null && dose.effectiveness >= 0.7
        }
        val moderate = doses.count { dose ->
            dose.effectiveness != null && dose.effectiveness in 0.4..0.69
        }
        val ineffective = doses.count { dose ->
            dose.effectiveness == null || dose.effectiveness < 0.4
        }

        val entries = mutableListOf<PieEntry>()
        if (effective > 0) entries.add(PieEntry(effective.toFloat(), "Effective"))
        if (moderate > 0) entries.add(PieEntry(moderate.toFloat(), "Moderate"))
        if (ineffective > 0) entries.add(PieEntry(ineffective.toFloat(), "Ineffective"))

        if (entries.isNotEmpty()) {
            val dataSet = PieDataSet(entries, "Dose Effectiveness")
            dataSet.colors = listOf(
                resources.getColor(R.color.success_green, null),
                resources.getColor(R.color.warning_orange, null),
                resources.getColor(R.color.error_red, null)
            )
            dataSet.valueTextSize = 12f
            dataSet.valueTextColor = resources.getColor(R.color.white, null)

            val pieData = PieData(dataSet)
            binding.pieChartEffectiveness.data = pieData
            binding.pieChartEffectiveness.invalidate()
        }
    }

    private fun updateStats(doses: List<InsulinDose>) {
        val totalDoses = doses.size
        val totalInsulin = doses.sumOf { it.calculatedDose.toDouble() }.toFloat()
        val avgDose = if (totalDoses > 0) totalInsulin / totalDoses else 0f
        val avgCarbs = doses.map { it.carbs }.average().toInt()

        // Calculate insulin-to-carb ratio
        val totalCarbs = doses.sumOf { it.carbs.toDouble() }.toInt()
        val ratio = if (totalInsulin > 0) {
            (totalCarbs / totalInsulin).toInt()
        } else 0

        binding.tvTotalDoses.text = totalDoses.toString()
        binding.tvTotalInsulin.text = String.format("%.1f units", totalInsulin)
        binding.tvAvgDose.text = String.format("%.1f units", avgDose)
        binding.tvAvgCarbs.text = "$avgCarbs g"
        binding.tvInsulinRatio.text = "1u : ${ratio}g"

        // Calculate effectiveness score
        val avgEffectiveness = doses.mapNotNull { it.effectiveness }.average()
        val effectivenessPercent = if (avgEffectiveness.isNaN()) 0 else (avgEffectiveness * 100).toInt()
        binding.tvEffectiveness.text = "$effectivenessPercent%"
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