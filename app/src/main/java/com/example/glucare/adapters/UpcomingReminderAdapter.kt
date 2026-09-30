package com.example.glucare.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.glucare.R
import com.example.glucare.models.CalendarEvent

class UpcomingReminderAdapter(
    private val events: List<CalendarEvent>
) : RecyclerView.Adapter<UpcomingReminderAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvWhen: TextView = view.findViewById(R.id.tvWhen)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_upcoming_reminder, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = events[position]

        // Set icon based on event type
        holder.ivIcon.setImageResource(event.getIcon())
        holder.ivIcon.setColorFilter(holder.itemView.context.getColor(event.getColor()))

        holder.tvTitle.text = event.title

        // Format time
        if (!event.time.isNullOrEmpty()) {
            holder.tvTime.text = event.time
        } else {
            holder.tvTime.text = "All day"
        }

        // Calculate when
        val now = System.currentTimeMillis()
        val eventTime = event.date + (event.time?.let { parseTimeToMillis(it) } ?: 0)
        val diffMinutes = ((eventTime - now) / (1000 * 60)).toInt()

        holder.tvWhen.text = when {
            diffMinutes < 0 -> "Overdue"
            diffMinutes < 60 -> "in $diffMinutes min"
            diffMinutes < 120 -> "in 1 hour"
            else -> "in ${diffMinutes / 60} hours"
        }
    }

    override fun getItemCount() = events.size

    private fun parseTimeToMillis(time: String): Long {
        return try {
            val parts = time.split(":")
            val hours = parts[0].toInt() * 60 * 60 * 1000
            val minutes = parts[1].toInt() * 60 * 1000
            (hours + minutes).toLong()
        } catch (e: Exception) {
            0L
        }
    }
}