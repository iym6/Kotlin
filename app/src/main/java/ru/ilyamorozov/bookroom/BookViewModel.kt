package ru.ilyamorozov.bookroom

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class BookViewModel(application: android.app.Application) : ViewModel() {
    private val repository: BookRepository

    init {
        try {
            val db = AppDatabase.getDatabase(application)
            repository = BookRepository(db.bookDao())
        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize database: ${e.message}", e)
        }
    }

    val newBooks: LiveData<List<Book>> get() = repository.getNewBooks()
    val readBooks: LiveData<List<Book>> get() = repository.getReadBooks()

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