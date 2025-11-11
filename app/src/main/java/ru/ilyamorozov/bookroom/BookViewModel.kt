package ru.ilyamorozov.bookroom

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class BookViewModel(application: android.app.Application) : ViewModel() {
    private val repository: BookRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BookRepository(db.bookDao())
    }

    val newBooks: LiveData<List<Book>> = repository.getNewBooks()
    val readBooks: LiveData<List<Book>> = repository.getReadBooks()

    fun addBook(book: Book) = viewModelScope.launch {
        repository.insert(book)
    }

    fun updateBook(book: Book) = viewModelScope.launch {
        repository.update(book)
    }

    fun deleteBook(book: Book) = viewModelScope.launch {
        repository.delete(book)
    }
}