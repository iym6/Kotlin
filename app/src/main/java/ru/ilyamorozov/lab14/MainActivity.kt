package ru.ilyamorozov.lab14

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.ilyamorozov.lab14.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Common.initCities(this)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, CityListFragment.newInstance())
                .commit()
        }
    }
}