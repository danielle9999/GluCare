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

class EventsAdapter(
    private var events: List<CalendarEvent> = listOf(),
    private val onLongClick: (CalendarEvent) -> Unit
) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    fun submitList(newEvents: List<CalendarEvent>) {
        events = newEvents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount() = events.size

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeContainer: LinearLayout = itemView.findViewById(R.id.timeContainer)
        private val tvEventTime: TextView = itemView.findViewById(R.id.tvEventTime)
        private val tvEventAmPm: TextView = itemView.findViewById(R.id.tvEventAmPm)
        private val iconContainer: LinearLayout = itemView.findViewById(R.id.iconContainer)
        private val ivEventIcon: ImageView = itemView.findViewById(R.id.ivEventIcon)
        private val tvEventTitle: TextView = itemView.findViewById(R.id.tvEventTitle)
        private val tvEventDescription: TextView = itemView.findViewById(R.id.tvEventDescription)
        private val tvEventLocation: TextView = itemView.findViewById(R.id.tvEventLocation)

        fun bind(event: CalendarEvent) {
            val context = itemView.context
            
            // Text values
            tvEventTitle.text = event.title
            tvEventDescription.text = event.description ?: ""
            
            // Time parsing
            if (!event.time.isNullOrEmpty()) {
                try {
                    val parts = event.time.split(":")
                    val hour = parts[0].toInt()
                    val min = parts[1]
                    val amPm = if (hour >= 12) "PM" else "AM"
                    val displayHour = if (hour % 12 == 0) 12 else hour % 12
                    tvEventTime.text = String.format("%02d:%s", displayHour, min)
                    tvEventAmPm.text = amPm
                } catch (e: Exception) {
                    tvEventTime.text = event.time
                    tvEventAmPm.text = ""
                }
            } else {
                tvEventTime.text = "--:--"
                tvEventAmPm.text = "ALL DAY"
            }

            // Location
            if (!event.location.isNullOrEmpty()) {
                tvEventLocation.text = event.location
                tvEventLocation.visibility = View.VISIBLE
            } else {
                tvEventLocation.visibility = View.GONE
            }

            // Colors and Icons from Model
            ivEventIcon.setImageResource(event.getIcon())
            val color = ContextCompat.getColor(context, event.getColor())
            ivEventIcon.setColorFilter(color)
            timeContainer.setBackgroundColor(color)
            
            itemView.setOnLongClickListener {
                onLongClick(event)
                true
            }
        }
    }
}
