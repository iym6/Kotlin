package ru.ilyamorozov.lab17

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment


class NewsFragment : Fragment() {
    private lateinit var clearButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_news, container, false)
        clearButton = view.findViewById(R.id.clearButton)

        clearButton.setOnClickListener {
            (activity as MainActivity).newsBadge.apply {
                number = 0
                isVisible = false
            }
        }
        return view
    }
}