package com.manak.eldercare

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    // UI elements
    private lateinit var tvSpo2: TextView
    private lateinit var tvHr: TextView
    private lateinit var tvTemp: TextView
    private lateinit var tvSpo2Status: TextView
    private lateinit var tvHrStatus: TextView
    private lateinit var tvTempStatus: TextView
    private lateinit var tvLastUpdate: TextView
    private lateinit var tvBattery: TextView
    private lateinit var btnRefresh: Button
    private lateinit var btnFall: Button
    private lateinit var btnSos: Button
    private lateinit var bottomNav: BottomNavigationView

    // Simulated values (replace with Firebase later)
    private var spo2 = 98.2f
    private var hr = 71f
    private var temp = 36.6f
    private var battery = 82

    private val handler = Handler(Looper.getMainLooper())

    // Auto refresh every 10 seconds
    private val autoRefreshRunnable = object : Runnable {
        override fun run() {
            updateVitals()
            handler.postDelayed(this, 10000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind views
        tvSpo2       = findViewById(R.id.tvSpo2)
        tvHr         = findViewById(R.id.tvHr)
        tvTemp       = findViewById(R.id.tvTemp)
        tvSpo2Status = findViewById(R.id.tvSpo2Status)
        tvHrStatus   = findViewById(R.id.tvHrStatus)
        tvTempStatus = findViewById(R.id.tvTempStatus)
        tvLastUpdate = findViewById(R.id.tvLastUpdate)
        tvBattery    = findViewById(R.id.tvBattery)
        btnRefresh   = findViewById(R.id.btnRefresh)
        btnFall      = findViewById(R.id.btnFall)
        btnSos       = findViewById(R.id.btnSos)
        bottomNav    = findViewById(R.id.bottomNav)

        // Refresh button
        btnRefresh.setOnClickListener {
            updateVitals()
            Toast.makeText(this, "Vitals refreshed", Toast.LENGTH_SHORT).show()
        }

        // Fall button
        btnFall.setOnClickListener {
            Toast.makeText(this, "⚠️ Fall Detected! Alert sent via LoRa to Doctor", Toast.LENGTH_LONG).show()
        }

        // SOS button
        btnSos.setOnClickListener {
            Toast.makeText(this, "🆘 SOS Activated! Caregiver notified", Toast.LENGTH_LONG).show()
        }

        // Bottom nav
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { true }
                R.id.nav_gps -> {
                    startActivity(android.content.Intent(this, GpsActivity::class.java))
                    true
                }
                R.id.nav_alerts -> {
                    startActivity(android.content.Intent(this, AlertsActivity::class.java))
                    true
                }
                R.id.nav_history -> {
                    startActivity(android.content.Intent(this, HistoryActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Start auto refresh
        handler.post(autoRefreshRunnable)
    }

    private fun updateVitals() {
        // Simulate small changes (replace with Firebase read later)
        spo2 = (spo2 + (-0.4f..0.4f).random()).coerceIn(93f, 100f)
        hr   = (hr   + (-2f..2f).random()).coerceIn(54f, 108f)
        temp = (temp + (-0.1f..0.1f).random()).coerceIn(35.8f, 38.8f)
        battery = (battery - 1).coerceAtLeast(0)

        // Update text
        tvSpo2.text   = "%.1f".format(spo2)
        tvHr.text     = hr.toInt().toString()
        tvTemp.text   = "%.1f".format(temp)
        tvBattery.text = "$battery%"

        // Update status badges
        tvSpo2Status.text = when {
            spo2 >= 97 -> "Normal"
            spo2 >= 95 -> "Low"
            else       -> "Critical"
        }
        tvHrStatus.text = when {
            hr in 60f..100f -> "Normal"
            hr > 100f       -> "High"
            else            -> "Low"
        }
        tvTempStatus.text = when {
            temp <= 37.5f -> "Normal"
            temp <= 38f   -> "Elevated"
            else          -> "Critical"
        }

        // Update timestamp
        val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        tvLastUpdate.text = "Last update: ${sdf.format(Date())} • every 10s"
    }

    private fun ClosedFloatingPointRange<Float>.random(): Float {
        return start + (Math.random() * (endInclusive - start)).toFloat()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(autoRefreshRunnable)
    }
}