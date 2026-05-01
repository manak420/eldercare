package com.manak.eldercare

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class Vitals(val spo2: Double, val hr: Double, val temp: Double)
data class GpsData(val dist: Double, val lat: Double, val lng: Double)
data class Alert(val id: Int, val type: String, val title: String, val body: String, val time: String)

class VitalsViewModel : ViewModel() {
    val vitals     = MutableLiveData(Vitals(98.1, 71.0, 36.6))
    val gps        = MutableLiveData(GpsData(1.2, 17.3852, 78.4869))
    val outside    = MutableLiveData(false)
    val battery    = MutableLiveData(82.0)
    val alerts     = MutableLiveData<MutableList<Alert>>(mutableListOf())
    val alertLog   = MutableLiveData<MutableList<Alert>>(mutableListOf())
    val connected  = MutableLiveData(true)
    private var alertId = 0

    fun pushAlert(type: String, title: String, body: String) {
        val id = ++alertId
        val time = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            .format(java.util.Date())
        val alert = Alert(id, type, title, body, time)
        alerts.value = (alerts.value ?: mutableListOf()).apply { add(0, this@VitalsViewModel.run { alert }) }
        alertLog.value = (alertLog.value ?: mutableListOf()).apply { add(0, alert) }
    }

    fun nextVitals() {
        val p = vitals.value ?: return
        val n = Vitals(
            spo2 = min(100.0, max(93.0, p.spo2 + Random.nextDouble(-0.4, 0.4))),
            hr   = min(108.0, max(54.0,  p.hr   + Random.nextDouble(-2.0, 2.0))),
            temp = min(38.8,  max(35.8,  p.temp + Random.nextDouble(-0.08, 0.12)))
        )
        vitals.value = n
        battery.value = max(5.0, (battery.value ?: 82.0) - 0.03)

        when {
            n.spo2 < 95  -> pushAlert("low",  "Low SpO₂",       "Blood oxygen ${String.format("%.1f", n.spo2)}% — below 95%")
            n.hr   > 100 -> pushAlert("high", "High Heart Rate", "Heart rate ${n.hr.toInt()} bpm")
            n.hr   < 55  -> pushAlert("low",  "Low Heart Rate",  "Heart rate ${n.hr.toInt()} bpm — check immediately")
            n.temp > 38  -> pushAlert("high", "Fever Detected",  "Temperature ${String.format("%.1f", n.temp)}°C")
        }
    }
}