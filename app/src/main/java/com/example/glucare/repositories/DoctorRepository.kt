package com.example.glucare.repositories

import android.content.Context
import android.util.Log
import com.example.glucare.models.Doctor
import org.json.JSONObject

class DoctorRepository(private val context: Context) {

    private var doctors: List<Doctor> = emptyList()

    init {
        loadDoctorsFromJson()
    }

    private fun loadDoctorsFromJson() {
        try {
            val jsonString = context.assets.open("doctors.json")
                .bufferedReader()
                .use { it.readText() }

            val root = JSONObject(jsonString)
            val jsonArray = root.getJSONArray("doctors")

            val list = mutableListOf<Doctor>()
            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)
                list.add(
                    Doctor(
                        id         = json.optInt("id", i),
                        name       = json.optString("name", ""),
                        specialty  = json.optString("specialty", ""),
                        address    = json.optString("address", ""),
                        phone      = json.optString("phone", ""),
                        wilaya     = json.optString("wilaya", ""),
                        wilayaCode = json.optInt("wilayaCode", 0),
                        latitude   = null,
                        longitude  = null
                    )
                )
            }
            doctors = list
            Log.d("DoctorRepository", "Loaded ${doctors.size} doctors")
        } catch (e: Exception) {
            Log.e("DoctorRepository", "Error loading doctors: ${e.message}", e)
            doctors = emptyList()
        }
    }

    fun getAvailableWilayas(): List<String> =
        listOf("All") + doctors.map { it.wilaya }.filter { it.isNotEmpty() }.distinct().sorted()

    fun getDoctorsByWilaya(wilaya: String): List<Doctor> =
        if (wilaya.isEmpty() || wilaya == "All") doctors
        else doctors.filter { it.wilaya.equals(wilaya, ignoreCase = true) }

    fun search(query: String): List<Doctor> {
        val q = query.lowercase().trim()
        if (q.isEmpty()) return doctors
        return doctors.filter {
            it.name.lowercase().contains(q) ||
                    it.specialty.lowercase().contains(q) ||
                    it.wilaya.lowercase().contains(q) ||
                    it.address.lowercase().contains(q)
        }
    }
}