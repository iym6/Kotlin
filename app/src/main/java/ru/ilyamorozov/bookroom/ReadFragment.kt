package ru.ilyamorozov.bookroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ReadFragment : Fragment() {

    private val viewModel: BookViewModel by activityViewModels()
    private lateinit var adapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_read, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_read)

        adapter = BookAdapter(
            onStatusClick = { book ->
                // Снимаем статус "прочитана", но сохраняем оценку, отзыв, страницы
                viewModel.updateBook(book.copy(isRead = false))
            },
            onEditClick = { (requireActivity() as MainActivity).showAddBookDialog(it) },
            onItemClick = { book ->
                (requireActivity() as MainActivity).showReviewDialog(book)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.readBooks.observe(viewLifecycleOwner) { adapter.submitList(it) }

        return view
    }
}