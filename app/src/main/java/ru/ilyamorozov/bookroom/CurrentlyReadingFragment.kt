package ru.ilyamorozov.bookroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CurrentlyReadingFragment : Fragment() {

    private val viewModel: BookViewModel by activityViewModels()
    private lateinit var adapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_currently_reading, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_currently_reading)

        adapter = BookAdapter(
            onStatusClick = { book ->
                // Переводим в "Прочитанные" → выбираем дату окончания
                (requireActivity() as MainActivity).showMarkAsReadDialog(book)
            },
            onEditClick = { (requireActivity() as MainActivity).showAddBookDialog(it) },
            onItemClick = { /* ничего */ }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.currentlyReadingBooks.observe(viewLifecycleOwner) { adapter.submitList(it) }

        return view
    }
}