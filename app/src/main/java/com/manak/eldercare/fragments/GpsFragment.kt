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

class GpsFragment : Fragment() {
    private val vm: VitalsViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_gps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvDist   = view.findViewById<TextView>(R.id.tvDistance)
        val tvStatus = view.findViewById<TextView>(R.id.tvGpsStatus)

        vm.gps.observe(viewLifecycleOwner) {
            tvDist.text = String.format("%.2f km from home", it.dist)
        }
        vm.outside.observe(viewLifecycleOwner) {
            tvStatus.text = if (it) "⚠️ OUTSIDE SAFE ZONE" else "✅ WITHIN SAFE ZONE"
            tvStatus.setTextColor(if (it) 0xFFFF6040.toInt() else 0xFF7EE8A2.toInt())
        }
    }
}