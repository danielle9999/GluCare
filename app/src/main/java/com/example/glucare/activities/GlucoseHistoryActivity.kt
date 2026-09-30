package com.example.glucare.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.glucare.GlucareApplication
import com.example.glucare.R
import com.example.glucare.models.GlucoseReading
import java.text.SimpleDateFormat
import java.util.*

class GlucoseHistoryActivity : BaseActivity() {

    private lateinit var app: GlucareApplication
    private lateinit var rv: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var tvAvg: TextView
    private lateinit var tvTotal: TextView
    private lateinit var tvTimeInRange: TextView
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            val errorMsg = e.message ?: "Unknown error"
            val stackTrace = e.stackTrace.take(5).joinToString("\n") { it.toString() }

            // Save to clipboard
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("error", "$errorMsg\n$stackTrace"))

            // Show in dialog
            AlertDialog.Builder(this)
                .setTitle("App Crashed")
                .setMessage("$errorMsg\n\nCopied to clipboard. Paste somewhere to see full error.")
                .setPositiveButton("OK") { _, _ -> finish() }
                .show()
        }
        setContentView(R.layout.activity_glucose_history)

        app = application as GlucareApplication

        rv = findViewById(R.id.rvGlucoseHistory)
        tvEmpty = findViewById(R.id.tvGlucoseHistoryEmpty)
        tvAvg = findViewById(R.id.tvGlucoseAvg)
        tvTotal = findViewById(R.id.tvGlucoseTotal)
        tvTimeInRange = findViewById(R.id.tvTimeInRange)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        rv.layoutManager = LinearLayoutManager(this)

        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val userId = app.sessionManager.getUserId()

        val readings = app.databaseHelper.getGlucoseReadings(userId)

        if (readings.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rv.visibility = View.GONE
            tvAvg.text = "-- mg/dL"
            tvTotal.text = "0 readings"
            tvTimeInRange.text = "No data"
        } else {
            tvEmpty.visibility = View.GONE
            rv.visibility = View.VISIBLE

            // FIXED: Safe average calculation
            val values = readings.map { it.value }
            val avg = if (values.isNotEmpty()) values.average() else 0.0

            val inRange = readings.count { it.value in 70..180 }
            val pct = if (readings.isNotEmpty()) (inRange * 100 / readings.size) else 0

            tvAvg.text = "${"%.0f".format(avg)} mg/dL"
            tvTotal.text = "${readings.size} reading${if (readings.size != 1) "s" else ""}"
            tvTimeInRange.text = "$pct% in range"

            rv.adapter = GlucoseHistoryAdapter(readings) { reading ->
                app.databaseHelper.writableDatabase.delete(
                    "glucose_readings", "id = ?", arrayOf(reading.id.toString())
                )
                loadData()
                Toast.makeText(this, "Deleted ${reading.value} mg/dL", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class GlucoseHistoryAdapter(
    private val items: List<GlucoseReading>,
    private val onDelete: (GlucoseReading) -> Unit
) : RecyclerView.Adapter<GlucoseHistoryAdapter.ViewHolder>() {

    private val sdf = SimpleDateFormat("EEE, MMM d · HH:mm", Locale.getDefault())

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvValue: TextView = itemView.findViewById(R.id.tvGlucoseValue)
        val tvDate: TextView = itemView.findViewById(R.id.tvGlucoseDate)
        val tvType: TextView = itemView.findViewById(R.id.tvGlucoseType)
        val tvStatus: TextView = itemView.findViewById(R.id.tvGlucoseStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_glucose_history, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val r = items[position]
        holder.tvValue.text = "${r.value} mg/dL"
        holder.tvDate.text = sdf.format(Date(r.timestamp * 1000))
        holder.tvType.text = r.readingType.name

        val (status, color) = when {
            r.value < 70 -> "LOW" to 0xFFF44336.toInt()
            r.value <= 140 -> "NORMAL" to 0xFF4CAF50.toInt()
            r.value <= 180 -> "ELEVATED" to 0xFFFF9800.toInt()
            else -> "HIGH" to 0xFFF44336.toInt()
        }
        holder.tvStatus.text = status
        holder.tvStatus.setTextColor(color)

        holder.itemView.setOnLongClickListener {
            onDelete(r)
            true
        }
    }
}