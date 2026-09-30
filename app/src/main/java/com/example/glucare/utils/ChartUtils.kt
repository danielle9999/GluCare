// utils/ChartUtils.kt
package com.example.glucare.utils

import android.content.Context
import com.example.glucare.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

object ChartUtils {

    fun styleLineChart(chart: LineChart, context: Context) {
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
        chart.setDrawGridBackground(false)

        // X axis
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.textColor = context.getColor(R.color.text_secondary)

        // Left axis
        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = context.getColor(R.color.light_gray)
        leftAxis.textColor = context.getColor(R.color.text_secondary)
        leftAxis.axisMinimum = 0f

        // Right axis
        chart.axisRight.isEnabled = false

        // Legend
        chart.legend.isEnabled = true
        chart.legend.textColor = context.getColor(R.color.text_primary)
    }

    fun styleBarChart(chart: BarChart, context: Context) {
        chart.description.isEnabled = false
        chart.setDrawGridBackground(false)
        chart.setPinchZoom(false)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.textColor = context.getColor(R.color.text_secondary)

        val leftAxis = chart.axisLeft
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = context.getColor(R.color.light_gray)
        leftAxis.textColor = context.getColor(R.color.text_secondary)
        leftAxis.axisMinimum = 0f

        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = true
    }

    fun stylePieChart(chart: PieChart, context: Context) {
        chart.description.isEnabled = false
        chart.setUsePercentValues(true)
        chart.setDrawHoleEnabled(true)
        chart.setHoleColor(context.getColor(R.color.white))
        chart.setTransparentCircleColor(context.getColor(R.color.white))
        chart.setTransparentCircleAlpha(110)
        chart.setHoleRadius(58f)
        chart.setTransparentCircleRadius(61f)
        chart.setDrawCenterText(true)
        chart.setRotationEnabled(true)
        chart.legend.isEnabled = true
        chart.legend.textColor = context.getColor(R.color.text_primary)
    }

    fun createGlucoseDataSet(entries: List<Entry>, label: String): LineDataSet {
        val dataSet = LineDataSet(entries, label)
        // FIX: these must be actual color integers, not resource IDs.
        // Call this only after you have a Context; pass resolved colors instead.
        // Use android.graphics.Color constants as safe defaults here.
        dataSet.color = android.graphics.Color.rgb(33, 150, 243)        // blue
        dataSet.setCircleColor(android.graphics.Color.rgb(33, 150, 243))
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.setDrawCircleHole(false)
        dataSet.valueTextSize = 10f
        dataSet.valueTextColor = android.graphics.Color.GRAY
        dataSet.setDrawFilled(true)
        dataSet.fillColor = android.graphics.Color.rgb(33, 150, 243)
        dataSet.fillAlpha = 50
        return dataSet
    }

    fun createInsulinDataSet(entries: List<BarEntry>, label: String): BarDataSet {
        val dataSet = BarDataSet(entries, label)
        dataSet.color = android.graphics.Color.rgb(76, 175, 80)         // green
        dataSet.valueTextColor = android.graphics.Color.GRAY
        dataSet.valueTextSize = 10f
        return dataSet
    }
}