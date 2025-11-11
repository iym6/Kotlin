package ru.ilyamorozov.bookroom

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val author: String,
    val title: String,

    val coverUrl: String? = null,
    val publisher: String? = null,
    val pageCount: Int? = null,
    val description: String? = null,

    //Статус
    val isRead: Boolean = false,
    val isCurrentlyReading: Boolean = false,  // ← НОВОЕ

    //Чтение
    val pagesRead: Int? = null,
    val rating: Float? = null,
    val review: String? = null,

    //Даты
    val startDate: Long? = null,  // ← timestamp
    val endDate: Long? = null     // ← timestamp
)