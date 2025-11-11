package ru.ilyamorozov.bookroom

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.lifecycle.LiveData

@Dao
interface BookDao {
    @Query("SELECT * FROM books WHERE isRead = 0 AND isCurrentlyReading = 0 ORDER BY id ASC")
    fun getNewBooks(): LiveData<List<Book>>

    @Query("SELECT * FROM books WHERE isCurrentlyReading = 1 ORDER BY startDate DESC")
    fun getCurrentlyReadingBooks(): LiveData<List<Book>>

    @Query("SELECT * FROM books WHERE isRead = 1 ORDER BY endDate DESC")
    fun getReadBooks(): LiveData<List<Book>>


    @Insert
    suspend fun insert(book: Book): Long

    @Update
    suspend fun update(book: Book)

    @Delete
    suspend fun delete(book: Book)
}