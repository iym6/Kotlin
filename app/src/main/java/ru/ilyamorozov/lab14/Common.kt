package ru.ilyamorozov.lab14

import android.content.Context
import com.example.cities.R

object Common {
    val cities = mutableListOf<City>()

    fun initCities(context: Context) {
        if (cities.isEmpty()) {
            val lines = context.resources.openRawResource(R.raw.cities)
                .bufferedReader().use { it.readLines() }

            for (i in 1 until lines.size) {
                val parts = lines[i].split(";")
                try {
                    cities.add(
                        City(
                            title = parts[3],
                            region = parts[2],
                            district = parts[1],
                            postalCode = parts[0],
                            timezone = parts[4],
                            population = parts[7],
                            founded = parts[8],
                            lat = parts[5].toFloat(),
                            lon = parts[6].toFloat()
                        )
                    )
                } catch (e: Exception) {
                    continue
                }
            }
            cities.sortBy { it.title }
        }
    }
}