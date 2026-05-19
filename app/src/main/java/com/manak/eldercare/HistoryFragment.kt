package com.manak.eldercare

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private val records = mutableListOf<VitalRecord>()
    private lateinit var database: DatabaseReference
    private val handler = Handler(Looper.getMainLooper())

    // Save normal vitals every 10 minutes
    private val normalHistoryRunnable = object : Runnable {
        override fun run() {
            saveNormalVitals()
            handler.postDelayed(this, 600000) // 10 minutes
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvHistory = view.findViewById(R.id.rvHistory)
        database  = FirebaseDatabase.getInstance("https://eldercare-84c09-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("history")

        adapter = HistoryAdapter(records)
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.adapter = adapter

        // Load history from Firebase
        listenForHistory()

        // Start 10 minute normal vitals save
        handler.post(normalHistoryRunnable)
    }

    private fun listenForHistory() {
        database.orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    records.clear()
                    for (child in snapshot.children) {
                        val spo2      = child.child("spo2").getValue(Double::class.java) ?: 0.0
                        val hr        = child.child("hr").getValue(Double::class.java) ?: 0.0
                        val temp      = child.child("temp").getValue(Double::class.java) ?: 0.0
                        val timestamp = child.child("timestamp").getValue(String::class.java) ?: ""
                        val type      = child.child("type").getValue(String::class.java) ?: "NORMAL"

//                        records.add(0, VitalRecord(
//                            time = timestamp,
//                            spo2 = "%.1f%%".format(spo2),
//                            hr   = "${hr.toInt()} bpm",
//                            temp = "%.1f°C".format(temp)
//                        ))
                        records.add(0, VitalRecord(
                            time = timestamp,
                            spo2 = "%.1f%%".format(spo2),
                            hr   = "${hr.toInt()} bpm",
                            temp = "%.1f°C".format(temp),
                            type = type
                        ))
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun saveNormalVitals() {
        // Read current vitals from Firebase and save to history
        FirebaseDatabase.getInstance("https://eldercare-84c09-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("vitals")
            .get().addOnSuccessListener { snapshot ->
                val spo2 = snapshot.child("spo2").getValue(Double::class.java) ?: 0.0
                val hr   = snapshot.child("hr").getValue(Double::class.java) ?: 0.0
                val temp = snapshot.child("temp").getValue(Double::class.java) ?: 0.0

                val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                val entry = mapOf(
                    "spo2"      to spo2,
                    "hr"        to hr,
                    "temp"      to temp,
                    "type"      to "NORMAL",
                    "timestamp" to sdf.format(Date())
                )
                database.push().setValue(entry)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(normalHistoryRunnable)
    }
}