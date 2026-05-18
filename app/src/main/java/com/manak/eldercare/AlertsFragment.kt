package com.manak.eldercare

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class AlertsFragment : Fragment() {

    private lateinit var rvAlerts: RecyclerView
    private lateinit var adapter: AlertAdapter
    private val alerts = mutableListOf<Alert>()
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alerts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvAlerts = view.findViewById(R.id.rvAlerts)
        database = FirebaseDatabase.getInstance("https://eldercare-84c09-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("vitals")
        // Clear all button
        view.findViewById<Button>(R.id.btnClearAll).setOnClickListener {
            database.removeValue()
            alerts.clear()
            adapter.notifyDataSetChanged()
        }

        // Setup adapter
        adapter = AlertAdapter(alerts)
        rvAlerts.layoutManager = LinearLayoutManager(requireContext())
        rvAlerts.adapter = adapter

        // Listen for alerts from Firebase
        listenForAlerts()
    }

    private fun listenForAlerts() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                alerts.clear()
                for (child in snapshot.children) {
                    val title     = child.child("title").getValue(String::class.java) ?: ""
                    val body      = child.child("body").getValue(String::class.java) ?: ""
                    val timestamp = child.child("timestamp").getValue(String::class.java) ?: ""

                    val emoji = when {
                        title.contains("SOS")  -> "🆘"
                        title.contains("Fall") -> "🚨"
                        title.contains("GPS")  -> "📍"
                        title.contains("SpO")  -> "📉"
                        title.contains("Heart")-> "📈"
                        title.contains("Temp") -> "🌡️"
                        else                   -> "✅"
                    }

                    val color = when {
                        title.contains("SOS")  -> 0xFFFF5252.toInt()
                        title.contains("Fall") -> 0xFFFFAA40.toInt()
                        title.contains("GPS")  -> 0xFF60B8FF.toInt()
                        title.contains("SpO")  -> 0xFFFFD060.toInt()
                        title.contains("Heart")-> 0xFFFF8060.toInt()
                        else                   -> 0xFF4ADE80.toInt()
                    }

                    alerts.add(0, Alert(emoji, title, body, timestamp, color))
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}