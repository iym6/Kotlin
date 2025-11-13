package ru.ilyamorozov.bookroom

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ReadFragment : Fragment() {

    private val viewModel: BookViewModel by activityViewModels()
    private lateinit var adapter: BookAdapter
    private lateinit var spinnerSort: Spinner
    private var currentSort = SortOption.DATE_DESC

    enum class SortOption { DATE_DESC, DATE_ASC, RATING_DESC, RATING_ASC }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_read, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_read)
        spinnerSort = view.findViewById(R.id.spinner_sort)

        adapter = BookAdapter(
            onLongClick = { book ->
                val root = requireActivity().findViewById<View>(R.id.root_layout)
                (requireActivity() as MainActivity).showContextMenuPopup(book, root)
            },
            onItemClick = { book ->
                (requireActivity() as MainActivity).showReviewDialog(book)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.readBooks.observe(viewLifecycleOwner) { books ->
            applySort(books)
        }

        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, position: Int, id: Long) {
                currentSort = when (position) {
                    0 -> SortOption.DATE_DESC
                    1 -> SortOption.DATE_ASC
                    2 -> SortOption.RATING_DESC
                    3 -> SortOption.RATING_ASC
                    else -> SortOption.DATE_DESC
                }
                applySort(viewModel.readBooks.value)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        return view
    }

    private fun applySort(books: List<Book>?) {
        if (books == null) return
        val sorted = when (currentSort) {
            SortOption.DATE_DESC -> books.sortedByDescending { it.endDate }
            SortOption.DATE_ASC -> books.sortedBy { it.endDate }
            SortOption.RATING_DESC -> books.sortedByDescending { it.rating }
            SortOption.RATING_ASC -> books.sortedBy { it.rating }
        }
        adapter.submitList(sorted)
    }
}