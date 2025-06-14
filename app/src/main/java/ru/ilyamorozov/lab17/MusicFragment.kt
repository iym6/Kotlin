package ru.ilyamorozov.lab17

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MusicFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_music, container, false)
        val tabs = view.findViewById<TabLayout>(R.id.tabs)
        val pager = view.findViewById<ViewPager2>(R.id.pager)

        val genres = resources.getStringArray(R.array.music_genres).toList()
        pager.adapter = MusicPagerAdapter(genres, this)

        TabLayoutMediator(tabs, pager) { tab, position ->
            tab.text = genres[position]
        }.attach()

        return view
    }
}