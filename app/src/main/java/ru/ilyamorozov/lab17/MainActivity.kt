package ru.ilyamorozov.lab17

import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private var isNewsVisible = false
    private lateinit var bottomNav: BottomNavigationView
    internal lateinit var newsBadge: BadgeDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNav = findViewById(R.id.bottomNav)
        newsBadge = bottomNav.getOrCreateBadge(R.id.menu_news)

        bottomNav.setOnItemSelectedListener { item ->
            isNewsVisible = item.itemId == R.id.menu_news
            when (item.itemId) {
                R.id.menu_music -> supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MusicFragment()).commit()
                R.id.menu_books -> supportFragmentManager.beginTransaction()
                    .replace(R.id.container, BooksFragment()).commit()
                R.id.menu_news -> supportFragmentManager.beginTransaction()
                    .replace(R.id.container, NewsFragment()).commit()
            }
            true
        }

        startNewsTimer()
    }

    private fun startNewsTimer() {
        object : CountDownTimer(Long.MAX_VALUE, 2000) {
            override fun onTick(millis: Long) {
                if (!isNewsVisible) {
                    runOnUiThread {
                        newsBadge.number++
                        newsBadge.isVisible = true
                    }
                }
            }
            override fun onFinish() {}
        }.start()
    }
}