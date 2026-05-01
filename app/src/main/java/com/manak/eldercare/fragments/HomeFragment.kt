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

class HomeFragment : Fragment() {
    private val vm: VitalsViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvSpo2 = view.findViewById<TextView>(R.id.tvSpo2)
        val tvHr   = view.findViewById<TextView>(R.id.tvHr)
        val tvTemp = view.findViewById<TextView>(R.id.tvTemp)

        vm.vitals.observe(viewLifecycleOwner) {
            tvSpo2.text = String.format("%.1f%%", it.spo2)
            tvHr.text   = "${it.hr.toInt()} bpm"
            tvTemp.text = String.format("%.1f°C", it.temp)
        }
    }
}