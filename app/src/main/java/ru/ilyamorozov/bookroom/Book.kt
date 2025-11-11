package ru.ilyamorozov.bookroom

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    //Обязательные поля
    val author: String,
    val title: String,
    //Необязательные поля
    val coverUrl: String? = null,
    val publisher: String? = null,
    val pageCount: Int? = null,
    val description: String? = null,
    //Статус
    val isRead: Boolean = false,
    val isCurrentlyReading: Boolean = false,
    //Отзыв
    val pagesRead: Int? = null,
    val rating: Float? = null,
    val review: String? = null,
    //Период
    val startDate: Long? = null,
    val endDate: Long? = null
)