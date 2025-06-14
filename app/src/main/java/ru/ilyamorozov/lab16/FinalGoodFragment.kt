package ru.ilyamorozov.lab16

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FinalGoodFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FinalGoodFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_final_good, container, false)

        view.findViewById<Button>(R.id.btn_run).setOnClickListener {
            findNavController().navigate(R.id.action_finalGoodFragment_to_introFragment)
        }

        return view
    }
}