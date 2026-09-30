package com.example.glucare.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.glucare.R
import com.example.glucare.adapters.DoctorAdapter
import com.example.glucare.databinding.ActivityDoctorsBinding
import com.example.glucare.models.Doctor
import com.example.glucare.repositories.DoctorRepository

class DoctorsActivity : BaseActivity() {

    private lateinit var binding: ActivityDoctorsBinding
    private lateinit var adapter: DoctorAdapter
    private lateinit var repo: DoctorRepository
    private var selectedWilaya: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.doctors)

        // Safe initialisation — DoctorRepository reads from assets; wrap in try/catch
        try {
            repo = DoctorRepository(this)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load doctors: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupRecyclerView()
        setupWilayaDropdown()
        setupSearch()
        setupFilterIcon()
        showEmptyState()          // start with empty state until a wilaya is selected
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupRecyclerView() {
        adapter = DoctorAdapter { doctor ->
            if (doctor.phone.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${doctor.phone}"))
                startActivity(intent)
            } else {
                Toast.makeText(this, "No phone number available", Toast.LENGTH_SHORT).show()
            }
        }
        binding.layoutDoctorsList.layoutManager = LinearLayoutManager(this)
        binding.layoutDoctorsList.adapter = adapter
    }

    private fun setupWilayaDropdown() {
        val wilayas = try { repo.getAvailableWilayas() } catch (e: Exception) { listOf("All") }

        val dropdownAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            wilayas
        )
        binding.actvWilaya.setAdapter(dropdownAdapter)

        binding.actvWilaya.setOnItemClickListener { _, _, position, _ ->
            selectedWilaya = wilayas[position]
            // Clear the text to show the selected wilaya properly
            binding.actvWilaya.setText(selectedWilaya, false)
            loadDoctors()
        }
    }

    private fun setupSearch() {
        binding.actvWilaya.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                val wilayas = repo.getAvailableWilayas()

                // If the query matches a Wilaya exactly → it's a Wilaya selection
                if (wilayas.any { it.equals(query, ignoreCase = true) }) {
                    selectedWilaya = query
                    loadDoctors()
                    return
                }

                // Otherwise, treat it as a doctor search
                when {
                    query.length >= 2 -> showResults(repo.search(query))
                    query.isEmpty()   -> showEmptyState()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Handle the search action on keyboard
        binding.actvWilaya.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.actvWilaya.text.toString().trim()
                if (query.length >= 2) {
                    showResults(repo.search(query))
                }
                true
            } else false
        }
    }
    private fun setupFilterIcon() {
        binding.ivFilter.setOnClickListener {
            binding.actvWilaya.setText("")
            binding.actvWilaya.showDropDown()
            showEmptyState()
        }
    }

    private fun loadDoctors() {
        val results = try {
            repo.getDoctorsByWilaya(selectedWilaya)
        } catch (e: Exception) {
            emptyList()
        }
        showResults(results)
    }

    private fun showResults(doctors: List<Doctor>) {
        if (doctors.isEmpty()) {
            binding.layoutDoctorsList.visibility = View.GONE
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.tvResultsCount.text = "No doctors found"
        } else {
            binding.layoutDoctorsList.visibility = View.VISIBLE
            binding.layoutEmpty.visibility = View.GONE
            val label = when {
                selectedWilaya.isNotEmpty() && selectedWilaya != "All" ->
                    "${doctors.size} doctor${if (doctors.size > 1) "s" else ""} in $selectedWilaya"
                else -> "${doctors.size} doctors found"
            }
            binding.tvResultsCount.text = label
            adapter.submitList(doctors)
        }
    }

    private fun showEmptyState() {
        binding.layoutDoctorsList.visibility = View.GONE
        binding.layoutEmpty.visibility = View.VISIBLE
        binding.tvResultsCount.text = "Select a wilaya to see doctors"
    }
}