package com.manak.eldercare

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.cardview.widget.CardView
import android.graphics.Color

private lateinit var safeZoneCard: CardView
private lateinit var tvZoneTitle: TextView
private lateinit var tvZoneBody: TextView

class HomeFragment : Fragment() {

    private lateinit var tvSpo2: TextView
    private lateinit var tvHr: TextView
    private lateinit var tvTemp: TextView
    private lateinit var tvSpo2Status: TextView
    private lateinit var tvHrStatus: TextView
    private lateinit var tvTempStatus: TextView
    private lateinit var tvLastUpdate: TextView
    private lateinit var tvBattery: TextView

    private lateinit var database: DatabaseReference
    private var vitalsListener: ValueEventListener? = null
    private var lastFall = false
    private var lastSos = false
    private var lastOutside = false
    private val DB_URL = "https://eldercare-84c09-default-rtdb.asia-southeast1.firebasedatabase.app"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvSpo2       = view.findViewById(R.id.tvSpo2)
        tvHr         = view.findViewById(R.id.tvHr)
        tvTemp       = view.findViewById(R.id.tvTemp)
        tvSpo2Status = view.findViewById(R.id.tvSpo2Status)
        tvHrStatus   = view.findViewById(R.id.tvHrStatus)
        tvTempStatus = view.findViewById(R.id.tvTempStatus)
        tvLastUpdate = view.findViewById(R.id.tvLastUpdate)
        tvBattery    = view.findViewById(R.id.tvBattery)
        safeZoneCard = view.findViewById(R.id.safeZoneCard)
        tvZoneTitle = view.findViewById(R.id.tvZoneTitle)
        tvZoneBody = view.findViewById(R.id.tvZoneBody)

        database = FirebaseDatabase.getInstance(DB_URL).getReference("vitals")

        view.findViewById<Button>(R.id.btnRefresh).setOnClickListener {
            Toast.makeText(requireContext(), "✓ Data is live!", Toast.LENGTH_SHORT).show()
        }


//        view.findViewById<Button>(R.id.btnFall).setOnClickListener {
//            Toast.makeText(requireContext(), "⚠️ Fall Detected! Alert sent via LoRa", Toast.LENGTH_LONG).show()
//        }
//        view.findViewById<Button>(R.id.btnSos).setOnClickListener {
//            Toast.makeText(requireContext(), "🆘 SOS Activated! Doctor notified", Toast.LENGTH_LONG).show()
//        }

        listenForVitals()
    }

    private fun listenForVitals() {
        vitalsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return
                android.util.Log.d("FIREBASE", "Data received: ${snapshot.value}")

                val spo2 = snapshot.child("spo2").getValue(Double::class.java) ?: 0.0
                val hr   = snapshot.child("hr").getValue(Double::class.java) ?: 0.0
                val temp = snapshot.child("temp").getValue(Double::class.java) ?: 0.0
                val sos  = snapshot.child("sos").getValue(Boolean::class.java) ?: false
                val fall = snapshot.child("fall").getValue(Boolean::class.java) ?: false
                val outsideZone = snapshot.child("outsideZone").getValue(Boolean::class.java) ?: false

                if (fall && !lastFall) {
                    pushAlert(
                        "🚨 Fall Detected",
                        "Sudden movement detected"
                    )
                }

                if (sos && !lastSos) {
                    pushAlert(
                        "🆘 SOS Alert",
                        "Emergency button pressed"
                    )
                }

                if (outsideZone && !lastOutside) {
                    pushAlert(
                        "📍 Outside Safe Zone",
                        "Elder moved outside safe area"
                    )
                }

                lastFall = fall
                lastSos = sos
                lastOutside = outsideZone
                if (outsideZone) {
                    safeZoneCard.setCardBackgroundColor(Color.parseColor("#3A1010"))
                    tvZoneTitle.text = "⚠️ Outside Safe Zone"
                    tvZoneTitle.setTextColor(Color.parseColor("#FF6B6B"))
                    tvZoneBody.text = "Elder has moved outside the 5 km safety radius."
                } else {
                    safeZoneCard.setCardBackgroundColor(Color.parseColor("#102A1A"))
                    tvZoneTitle.text = "✅ Inside Safe Zone"
                    tvZoneTitle.setTextColor(Color.parseColor("#4ADE80"))
                    tvZoneBody.text = "Elder is within the 5 km safety radius."
                }

                tvSpo2.text = "%.1f".format(spo2)
                tvHr.text   = hr.toInt().toString()
                tvTemp.text = "%.1f".format(temp)

                tvSpo2Status.text = when {
                    spo2 >= 97 -> "Normal"
                    spo2 >= 95 -> "Low"
                    else       -> "Critical"
                }
                tvHrStatus.text = when {
                    hr in 50.0..100.0 -> "Normal"
                    hr > 100          -> "High"
                    else              -> "Low"
                }
                tvTempStatus.text = when {
                    temp <= 37.5 -> "Normal"
                    temp <= 38.0 -> "Elevated"
                    else         -> "Critical"
                }

                val sdf = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
                tvLastUpdate.text = "Last update: ${sdf.format(Date())} • live"

                if (sos)  saveToAlerts("🆘 SOS Alert",    "Elder pressed SOS button")
                if (fall) saveToAlerts("🚨 Fall Detected", "MPU6050 detected sudden fall")

                if (spo2 < 95 || hr > 100 || hr < 50 || temp > 38) {
                    saveToHistory(spo2, hr, temp, "CRITICAL")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return
                android.util.Log.e("FIREBASE", "Error: ${error.message}")
                Toast.makeText(requireContext(), "Connection lost: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // THIS IS THE KEY LINE — attach the listener
        database.addValueEventListener(vitalsListener!!)
    }

    private fun pushAlert(title: String, body: String) {

        android.util.Log.d("AUTO_ALERT", "Alert pushed: $title")

        val sdf = java.text.SimpleDateFormat(
            "dd MMM hh:mm a",
            java.util.Locale.getDefault()
        )

        val alert = mapOf(
            "title" to title,
            "body" to body,
            "timestamp" to sdf.format(java.util.Date())
        )

        FirebaseDatabase
            .getInstance("https://eldercare-84c09-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("alerts")
            .push()
            .setValue(alert)
        showLocalNotification(title, body)
    }


    private fun showLocalNotification(title: String, body: String) {

        val builder = androidx.core.app.NotificationCompat.Builder(
            requireContext(),
            "eldercare_alerts"
        )
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val manager =
            requireContext().getSystemService(
                android.content.Context.NOTIFICATION_SERVICE
            ) as android.app.NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            val channel = android.app.NotificationChannel(
                "eldercare_alerts",
                "ElderCare Alerts",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )

            manager.createNotificationChannel(channel)
        }

        manager.notify(
            System.currentTimeMillis().toInt(),
            builder.build()
        )
    }

    private fun saveToHistory(spo2: Double, hr: Double, temp: Double, type: String) {
        val historyRef = FirebaseDatabase.getInstance(DB_URL).getReference("history")
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        historyRef.push().setValue(mapOf(
            "spo2"      to spo2,
            "hr"        to hr,
            "temp"      to temp,
            "type"      to type,
            "timestamp" to sdf.format(Date())
        ))
    }

    private fun saveToAlerts(title: String, body: String) {
        val alertsRef = FirebaseDatabase.getInstance(DB_URL).getReference("alerts")
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        alertsRef.push().setValue(mapOf(
            "title"     to title,
            "body"      to body,
            "timestamp" to sdf.format(Date())
        ))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vitalsListener?.let { database.removeEventListener(it) }
    }
}