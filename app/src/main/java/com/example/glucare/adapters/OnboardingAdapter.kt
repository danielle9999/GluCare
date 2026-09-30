package com.example.glucare.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.glucare.R

class OnboardingAdapter(
    private val titles: List<String>,
    private val descriptions: List<String>,
    private val icons: List<Int>
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // These IDs must match what's in your activity_onboarding.xml
        val tvTitle: TextView = itemView.findViewById(R.id.tvOnboardingTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvOnboardingDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_unboarding_slide, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.tvTitle.text = titles[position]
        holder.tvDescription.text = descriptions[position]
    }

    override fun getItemCount() = titles.size
}