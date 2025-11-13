package ru.ilyamorozov.bookroom

import android.app.AlertDialog
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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_currently_reading, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_currently_reading)

        adapter = BookAdapter(
            onLongClick = { book ->
                val root = requireActivity().findViewById<View>(R.id.root_layout)
                (requireActivity() as MainActivity).showContextMenuPopup(book, root)
            },
            onItemClick = { }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.currentlyReadingBooks.observe(viewLifecycleOwner) { books ->
            adapter.submitList(books)
        }

        return view
    }
}