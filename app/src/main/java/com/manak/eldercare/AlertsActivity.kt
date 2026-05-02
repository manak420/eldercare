package com.manak.eldercare

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class Alert(
    val emoji: String,
    val title: String,
    val body: String,
    val time: String,
    val titleColor: Int
)

class AlertsActivity : AppCompatActivity() {

    private lateinit var rvAlerts: RecyclerView
    private lateinit var adapter: AlertAdapter
    private val alerts = mutableListOf<Alert>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alerts)

        rvAlerts = findViewById(R.id.rvAlerts)

        // Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Clear all button
        findViewById<android.widget.Button>(R.id.btnClearAll).setOnClickListener {
            alerts.clear()
            adapter.notifyDataSetChanged()
        }

        // Load sample alerts (replace with Firebase later)
        loadSampleAlerts()

        // Setup RecyclerView
        adapter = AlertAdapter(alerts)
        rvAlerts.layoutManager = LinearLayoutManager(this)
        rvAlerts.adapter = adapter
    }

    private fun loadSampleAlerts() {
        alerts.add(Alert("🆘", "SOS Activated", "Manual SOS button pressed by Amma", "10:45 PM", 0xFFFF5252.toInt()))
        alerts.add(Alert("🚨", "Fall Detected!", "MPU6050 detected sudden impact. LoRa alert sent to Doctor.", "10:30 PM", 0xFFFFAA40.toInt()))
        alerts.add(Alert("📍", "Outside Safe Zone", "Amma moved 5.3 km from home", "09:15 AM", 0xFF60B8FF.toInt()))
        alerts.add(Alert("📉", "Low SpO₂", "Blood oxygen dropped to 93.5%", "08:50 AM", 0xFFFFD060.toInt()))
        alerts.add(Alert("📈", "High Heart Rate", "Heart rate reached 105 bpm", "08:20 AM", 0xFFFF8060.toInt()))
        alerts.add(Alert("✅", "Back Home", "Amma returned within safe zone", "11:00 AM", 0xFF4ADE80.toInt()))
    }
}

class AlertAdapter(private val alerts: MutableList<Alert>) :
    RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    class AlertViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEmoji: TextView = view.findViewById(R.id.tvAlertEmoji)
        val tvTitle: TextView = view.findViewById(R.id.tvAlertTitle)
        val tvBody: TextView = view.findViewById(R.id.tvAlertBody)
        val tvTime: TextView = view.findViewById(R.id.tvAlertTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alert, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]
        holder.tvEmoji.text = alert.emoji
        holder.tvTitle.text = alert.title
        holder.tvTitle.setTextColor(alert.titleColor)
        holder.tvBody.text = alert.body
        holder.tvTime.text = alert.time
    }

    override fun getItemCount() = alerts.size
}