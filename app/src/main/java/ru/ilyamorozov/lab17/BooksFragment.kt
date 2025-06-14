package ru.ilyamorozov.lab17


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout

class BooksFragment : Fragment() {
    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_books, container, false)
        val tabs = view.findViewById<TabLayout>(R.id.tabs)
        val tabInfo = view.findViewById<TextView>(R.id.tabInfo)

        tabs.addTab(tabs.newTab().setText(getString(R.string.tab_new)))
        tabs.addTab(tabs.newTab().setText(getString(R.string.tab_read)))

        tabInfo.text = "Выбрано: ${getString(R.string.tab_new)}"

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tabInfo.text = "Выбрано: ${tab?.text}"
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        return view
    }
}