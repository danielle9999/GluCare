package com.example.glucare.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.glucare.R
import com.example.glucare.models.Doctor

class DoctorAdapter(
    private val onCallClick: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorAdapter.ViewHolder>() {

    private var doctors: List<Doctor> = emptyList()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvInitials: TextView = itemView.findViewById(R.id.tvDoctorInitials)
        val tvName: TextView = itemView.findViewById(R.id.tvDoctorName)
        val tvSpecialty: TextView = itemView.findViewById(R.id.tvDoctorSpecialty)
        val tvAddress: TextView = itemView.findViewById(R.id.tvDoctorAddress)
        val tvPhone: TextView = itemView.findViewById(R.id.tvDoctorPhone)
        val ivCall: ImageView = itemView.findViewById(R.id.ivDoctorCall)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doctor = doctors[position]

        holder.tvInitials.text = doctor.initials()
        holder.tvName.text = doctor.name
        holder.tvSpecialty.text = doctor.specialty
        holder.tvAddress.text = doctor.address
        holder.tvPhone.text = doctor.phone.ifEmpty { "No phone" }

        holder.ivCall.setOnClickListener {
            onCallClick(doctor)
        }
    }

    override fun getItemCount() = doctors.size

    fun submitList(newList: List<Doctor>) {
        doctors = newList
        notifyDataSetChanged()
    }
}