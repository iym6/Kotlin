package ru.ilyamorozov.lab16

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class IntroFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_intro, container, false)
        val btnRun = view.findViewById<Button>(R.id.btn_run)
        val btnStay = view.findViewById<Button>(R.id.btn_stay)
        btnRun.setOnClickListener {
            findNavController().navigate(R.id.action_introFragment_to_hareFragment)
        }
        btnStay.setOnClickListener {
            findNavController().navigate(R.id.action_introFragment_to_finalBadFragment)
        }
        return view
    }
}