package ru.ilyamorozov.bookroom

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("volumes")
    suspend fun getBookGoogle(
        @Query("q") query: String,
        @Query("langRestrict") lang: String = "ru",
        @Query("country") country: String = "RU"
    ): GoogleBookResponse

    companion object {
        private val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/books/v1/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()

        fun create(): ApiService = retrofit.create(ApiService::class.java)
    }
}