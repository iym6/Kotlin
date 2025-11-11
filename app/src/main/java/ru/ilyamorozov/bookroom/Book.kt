package ru.ilyamorozov.bookroom

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // Основные
    val author: String,
    val title: String,

    // Новые поля
    val coverUrl: String? = null,
    val publisher: String? = null,
    val pageCount: Int? = null,
    val description: String? = null,

    // Прочтение
    val isRead: Boolean = false,
    val pagesRead: Int? = null,        // сколько прочитано
    val rating: Float? = null,         // 0.0f .. 10.0f
    val review: String? = null
)