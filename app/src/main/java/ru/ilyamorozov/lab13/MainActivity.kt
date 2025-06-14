package ru.ilyamorozov.lab13

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import ru.ilyamorozov.lab13.databinding.ActivityMainBinding
import androidx.core.net.toUri

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var selectedCity: City? = null

    private val cityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getIntExtra("cityIndex", -1)?.let { index ->
                if (index != -1) {
                    selectedCity = Common.cities[index]
                    updateCityInfo()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Common.initCities(this)

        // Установка текста кнопок из ресурсов
        binding.btnSelectCity.text = getString(R.string.choose_city)
        binding.btnShowOnMap.text = getString(R.string.show_on_map)

        binding.btnSelectCity.setOnClickListener {
            val intent = Intent(this, CityListActivity::class.java)
            cityLauncher.launch(intent)
        }

        binding.btnShowOnMap.setOnClickListener {
            selectedCity?.let { city ->
                showCityOnMap(city)
            } ?: run {
                Toast.makeText(this, R.string.select_city_first, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateCityInfo() {
        selectedCity?.let { city ->
            binding.tvCity.text = getString(R.string.city_template, city.title)
            binding.tvDistrict.text = getString(R.string.district_template, city.district)
            binding.tvRegion.text = getString(R.string.region_template, city.region)
            binding.tvPostalCode.text = getString(R.string.postal_code_template, city.postalCode)
            binding.tvTimezone.text = getString(R.string.timezone_template, city.timezone)
            binding.tvPopulation.text = getString(R.string.population_template, city.population)
            binding.tvFounded.text = getString(R.string.founded_template, city.founded)
        }
    }

    private fun showCityOnMap(city: City) {
        val uri = "geo:${city.lat},${city.lon}?q=${city.lat},${city.lon}(${city.title})".toUri()
        val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            startActivity(mapIntent)
        } catch (e: ActivityNotFoundException) {
            mapIntent.setPackage(null)
            try {
                startActivity(mapIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, R.string.no_apps_found, Toast.LENGTH_LONG).show()
            }
        }
    }
}