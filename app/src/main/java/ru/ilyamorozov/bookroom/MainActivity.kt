package ru.ilyamorozov.bookroom

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: BookViewModel
    private lateinit var adapterNew: BookAdapter
    private lateinit var adapterRead: BookAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerNew = findViewById<RecyclerView>(R.id.recycler_new_books)
        val recyclerRead = findViewById<RecyclerView>(R.id.recycler_read_books)
        val btnAddBook = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.btn_add_book)

        val factory = BookViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory)[BookViewModel::class.java]

        adapterNew = BookAdapter(
            onStatusClick = { toggleStatus(it) },
            onEditClick = { showAddBookDialog(it) }
        )
        adapterRead = BookAdapter(
            onStatusClick = { toggleStatus(it) },
            onEditClick = { showAddBookDialog(it) }
        )

        recyclerNew.layoutManager = LinearLayoutManager(this)
        recyclerNew.adapter = adapterNew

        recyclerRead.layoutManager = LinearLayoutManager(this)
        recyclerRead.adapter = adapterRead

        btnAddBook.setOnClickListener { showAddBookDialog() }

        viewModel.newBooks.observe(this) { adapterNew.submitList(it) }
        viewModel.readBooks.observe(this) { adapterRead.submitList(it) }
    }

    private fun showAddBookDialog(book: Book? = null) {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_book, null)
        builder.setView(view)

        val editTextAuthor = view.findViewById<EditText>(R.id.editTextAuthor)
        val editTextTitle = view.findViewById<EditText>(R.id.editTextTitle)
        val checkBoxStatus = view.findViewById<CheckBox>(R.id.checkBoxStatus)

        book?.let {
            editTextAuthor.setText(it.author)
            editTextTitle.setText(it.title)
            checkBoxStatus.isChecked = it.status == 1
        }

        builder.setTitle(if (book == null) getString(R.string.addBook) else getString(R.string.editBook))

        builder.setPositiveButton(getString(R.string.save)) { _, _ ->
            val author = editTextAuthor.text.toString().trim()
            val title = editTextTitle.text.toString().trim()
            val status = if (checkBoxStatus.isChecked) 1 else 0

            if (author.isNotEmpty() && title.isNotEmpty()) {
                val newBook = book?.copy(author = author, title = title, status = status)
                    ?: Book(author = author, title = title, status = status)

                if (book == null) {
                    viewModel.addBook(newBook)
                } else {
                    viewModel.updateBook(newBook)
                }
            }
        }

        if (book != null) {
            builder.setNegativeButton(getString(R.string.delete)) { _, _ ->
                viewModel.deleteBook(book)
            }
        } else {
            builder.setNegativeButton(getString(R.string.cancel), null)
        }

        builder.show()
    }

    private fun toggleStatus(book: Book) {
        val updated = book.copy(status = if (book.status == 0) 1 else 0)
        viewModel.updateBook(updated)
    }
}