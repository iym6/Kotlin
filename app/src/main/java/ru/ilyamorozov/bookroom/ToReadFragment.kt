package ru.ilyamorozov.bookroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ToReadFragment : Fragment() {

    private val viewModel: BookViewModel by activityViewModels()
    private lateinit var adapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_to_read, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_to_read)

        adapter = BookAdapter(
            onStatusClick = { toggleStatus(it) },
            onEditClick = { (requireActivity() as MainActivity).showAddBookDialog(it) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.newBooks.observe(viewLifecycleOwner) { books ->
            adapter.submitList(books)
        }

        return view
    }

    private fun toggleStatus(book: Book) {
        val updated = book.copy(isRead = !book.isRead)
        viewModel.updateBook(updated)
    }
}