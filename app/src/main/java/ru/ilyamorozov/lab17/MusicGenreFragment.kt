package ru.ilyamorozov.lab17

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class MusicGenreFragment : Fragment() {
    companion object {
        fun newInstance(genre: String) = MusicGenreFragment().apply {
            arguments = Bundle().apply { putString("genre", genre) }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val genre = arguments?.getString("genre") ?: ""
        val textView = TextView(requireContext())
        textView.text = "Жанр: $genre"
        textView.gravity = Gravity.CENTER
        return textView
    }
}