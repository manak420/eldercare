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

class HistoryFragment : Fragment() {
    private val vm: VitalsViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tvLog = view.findViewById<TextView>(R.id.tvHistoryLog)
        vm.alertLog.observe(viewLifecycleOwner) {
            tvLog.text = if (it.isEmpty()) "📋 No history yet"
            else it.joinToString("\n\n") { a -> "[${a.time}] ${a.title}\n${a.body}" }
        }
    }
}