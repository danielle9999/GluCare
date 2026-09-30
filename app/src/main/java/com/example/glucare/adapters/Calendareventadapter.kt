package com.example.glucare.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.glucare.R
import com.example.glucare.models.CalendarEvent

class CalendarEventAdapter(
    private val events: MutableList<CalendarEvent>,
    private val onLongClick: (CalendarEvent) -> Unit
) : RecyclerView.Adapter<CalendarEventAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeContainer: LinearLayout = view.findViewById(R.id.timeContainer)
        val tvTime: TextView = view.findViewById(R.id.tvEventTime)
        val tvAmPm: TextView = view.findViewById(R.id.tvEventAmPm)
        val iconContainer: LinearLayout = view.findViewById(R.id.iconContainer)
        val ivIcon: ImageView = view.findViewById(R.id.ivEventIcon)
        val tvTitle: TextView = view.findViewById(R.id.tvEventTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvEventDescription)
        val tvLocation: TextView = view.findViewById(R.id.tvEventLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_event, parent, false))

    override fun getItemCount() = events.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = events[position]
        val context = holder.itemView.context

        holder.tvTitle.text = event.title
        holder.tvDescription.text = event.description ?: ""
        
        // Time parsing
        if (!event.time.isNullOrEmpty()) {
            try {
                val parts = event.time.split(":")
                val hour = parts[0].toInt()
                val min = parts[1]
                val amPm = if (hour >= 12) "PM" else "AM"
                val displayHour = if (hour % 12 == 0) 12 else hour % 12
                holder.tvTime.text = String.format("%02d:%s", displayHour, min)
                holder.tvAmPm.text = amPm
            } catch (e: Exception) {
                holder.tvTime.text = event.time
                holder.tvAmPm.text = ""
            }
        } else {
            holder.tvTime.text = "--:--"
            holder.tvAmPm.text = "ALL DAY"
        }

        // Location
        if (!event.location.isNullOrEmpty()) {
            holder.tvLocation.text = event.location
            holder.tvLocation.visibility = View.VISIBLE
        } else {
            holder.tvLocation.visibility = View.GONE
        }

        // Icons and Colors from Model
        holder.ivIcon.setImageResource(event.getIcon())
        val color = ContextCompat.getColor(context, event.getColor())
        holder.ivIcon.setColorFilter(color)
        holder.timeContainer.setBackgroundColor(color)
        
        // Icon container background (lighter version or just transparent)
        holder.iconContainer.alpha = 0.9f

        holder.itemView.setOnLongClickListener {
            onLongClick(event)
            true
        }
    }

    fun updateData(newEvents: List<CalendarEvent>) {
        events.clear()
        events.addAll(newEvents)
        notifyDataSetChanged()
    }
}
