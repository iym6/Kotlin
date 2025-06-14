package ru.ilyamorozov.lab15

data class ShoppingItem(
    val id: Long = System.currentTimeMillis(),
    var name: String,
    var quantity: String
)
