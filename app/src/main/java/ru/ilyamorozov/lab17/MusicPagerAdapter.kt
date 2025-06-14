package ru.ilyamorozov.lab17

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MusicPagerAdapter(
    private val genres: List<String>,
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = genres.size

    override fun createFragment(position: Int): Fragment {
        return MusicGenreFragment.newInstance(genres[position])
    }
}