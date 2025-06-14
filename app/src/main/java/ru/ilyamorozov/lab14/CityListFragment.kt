package ru.ilyamorozov.lab14

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import ru.ilyamorozov.lab14.databinding.FragmentCityListBinding

class CityListFragment : Fragment() {
    private var _binding: FragmentCityListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCityListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = CityAdapter(Common.cities) { city ->
            val fragment = CityDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("title", city.title)
                    putString("region", city.region)
                    putString("district", city.district)
                    putString("postalCode", city.postalCode)
                    putString("timezone", city.timezone)
                    putString("population", city.population)
                    putString("founded", city.founded)
                    putFloat("lat", city.lat)
                    putFloat("lon", city.lon)
                }
            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}