package ru.ilyamorozov.bookroom

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val author: String,
    val title: String,
    val status: Int
)