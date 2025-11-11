package ru.ilyamorozov.bookroom

data class GoogleBookResponse(val items: List<GoogleBookItem>?)
data class GoogleBookItem(val volumeInfo: VolumeInfo)
data class VolumeInfo(
    val title: String?,
    val authors: List<String>?,
    val publisher: String?,
    val publishedDate: String?,
    val pageCount: Int?,
    val description: String?,
    val imageLinks: ImageLinks?
)
data class ImageLinks(val thumbnail: String?)