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
import android.graphics.Color

//data class VitalRecord(
//    val time: String,
//    val spo2: String,
//    val hr: String,
//    val temp: String
//)
data class VitalRecord(
    val time: String,
    val spo2: String,
    val hr: String,
    val temp: String,
    val type: String
)

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        rvHistory = findViewById(R.id.rvHistory)

        // Back button
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Sample history data (replace with Firebase later)
        val records = mutableListOf(
            VitalRecord("11:00 PM", "98.2%", "71 bpm", "36.6°C", "NORMAL"),
            VitalRecord("10:50 PM", "97.8%", "74 bpm", "36.7°C", "NORMAL"),
            VitalRecord("10:40 PM", "98.0%", "70 bpm", "36.5°C", "NORMAL"),
            VitalRecord("10:30 PM", "97.5%", "76 bpm", "36.8°C", "NORMAL"),
            VitalRecord("10:20 PM", "98.3%", "72 bpm", "36.6°C", "NORMAL"),

            VitalRecord("10:10 PM", "96.8%", "80 bpm", "37.0°C", "WARNING"),

            VitalRecord("10:00 PM", "95.2%", "88 bpm", "37.4°C", "CRITICAL"),

            VitalRecord("09:50 PM", "97.1%", "75 bpm", "36.9°C", "NORMAL"),
            VitalRecord("09:40 PM", "98.4%", "69 bpm", "36.5°C", "NORMAL"),
            VitalRecord("09:30 PM", "98.1%", "71 bpm", "36.6°C", "NORMAL"),
        )

        val adapter = HistoryAdapter(records)
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = adapter
    }
}

class HistoryAdapter(private val records: List<VitalRecord>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView  = view.findViewById(R.id.tvHistoryTime)
        val tvSpo2: TextView  = view.findViewById(R.id.tvHistorySpo2)
        val tvHr: TextView    = view.findViewById(R.id.tvHistoryHr)
        val tvTemp: TextView  = view.findViewById(R.id.tvHistoryTemp)
        val tvStatus: TextView = view.findViewById(R.id.tvHistoryStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val record = records[position]
        holder.tvTime.text = record.time
        holder.tvSpo2.text = record.spo2
        holder.tvHr.text   = record.hr
        holder.tvTemp.text = record.temp
        holder.tvStatus.text = record.type

        holder.tvStatus.text = record.type

        when (record.type) {

            "CRITICAL" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#4AFF5252"))
                holder.tvStatus.setBackgroundResource(R.drawable.badge_bg_red)
            }

            "WARNING" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#4AFFD060"))
                holder.tvStatus.setBackgroundResource(R.drawable.badge_bg_yellow)
            }

            else -> {
                holder.tvStatus.setTextColor(Color.parseColor("#4ADE80"))
                holder.tvStatus.setBackgroundResource(R.drawable.badge_bg_green)
            }
        }
    }

    override fun getItemCount() = records.size
}