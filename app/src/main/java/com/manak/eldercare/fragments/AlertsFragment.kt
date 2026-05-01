package com.manak.eldercare.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.manak.eldercare.R
import com.manak.eldercare.VitalsViewModel

class AlertsFragment : Fragment() {
    private val vm: VitalsViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_alerts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvAlerts = view.findViewById<TextView>(R.id.tvAlertCount)
        vm.alerts.observe(viewLifecycleOwner) {
            tvAlerts.text = if (it.isEmpty()) "🔔 No active alerts"
            else "🔴 ${it.size} active alert(s)\n\n${it.take(3).joinToString("\n\n") { a -> "• ${a.title}\n  ${a.body}" }}"
        }
    }
}