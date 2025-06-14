package ru.ilyamorozov.lab13

import android.content.Context
import android.util.Log

object Common {
    val cities = mutableListOf<City>()

    fun initCities(ctx: Context) {
        if (cities.isEmpty()) {
            try {
                val lines = ctx.resources.openRawResource(R.raw.cities)
                    .bufferedReader().readLines()

                for (i in 1 until lines.size) {
                    val parts = lines[i].split(";")
                    cities.add(City(
                        parts[3], parts[2], parts[1], parts[0],
                        parts[4], parts[7], parts[8],
                        parts[5].toFloat(), parts[6].toFloat()
                    ))
                }
                cities.sortBy { it.title }
            } catch (e: Exception) {
                Log.e("Common", "Error reading cities data", e)
            }
        }
    }
}