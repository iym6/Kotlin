package ru.ilyamorozov.lab14

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import ru.ilyamorozov.lab14.databinding.FragmentCityDetailBinding

class CityDetailFragment : Fragment() {
    private var _binding: FragmentCityDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCityDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val let = arguments?.let { args ->
            binding.tvCity.text = "Город: ${args.getString("title")}"
            binding.tvDistrict.text = "Федеральный округ: ${args.getString("district")}"
            binding.tvRegion.text = "Регион: ${args.getString("region")}"
            binding.tvPostalCode.text = "Почтовый индекс: ${args.getString("postalCode")}"
            binding.tvTimezone.text = "Часовой пояс: ${args.getString("timezone")}"
            binding.tvPopulation.text = "Население: ${args.getString("population")}"
            binding.tvFounded.text = "Основан: ${args.getString("founded")}"

            binding.btnShowOnMap.setOnClickListener {
                val uri = "geo:${args.getFloat("lat")},${args.getFloat("lon")}"
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Не найдено приложения для карт",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}