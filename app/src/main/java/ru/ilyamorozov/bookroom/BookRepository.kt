package ru.ilyamorozov.bookroom

import androidx.lifecycle.LiveData

class BookRepository(private val bookDao: BookDao) {
    fun getNewBooks(): LiveData<List<Book>> = bookDao.getNewBooks()
    fun getReadBooks(): LiveData<List<Book>> = bookDao.getReadBooks()

    suspend fun insert(book: Book) = bookDao.insert(book)
    suspend fun update(book: Book) = bookDao.update(book)
    suspend fun delete(book: Book) = bookDao.delete(book)
}