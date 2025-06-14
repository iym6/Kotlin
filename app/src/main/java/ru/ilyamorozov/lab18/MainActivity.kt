package ru.ilyamorozov.lab18

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.net.URL
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressBar = findViewById(R.id.progressBar)
        listView = findViewById(R.id.listView)

        findViewById<View>(R.id.btnLoad).setOnClickListener {
            loadCurrencyRates()
        }
    }

    private fun loadCurrencyRates() {
        progressBar.visibility = View.VISIBLE
        listView.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val xml = URL("https://www.cbr.ru/scripts/XML_daily.asp")
                    .readText(Charset.forName("Windows-1251"))

                val currencyList = parseXml(xml)

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    listView.visibility = View.VISIBLE
                    listView.adapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_list_item_1,
                        currencyList
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    listView.visibility = View.VISIBLE
                    listView.adapter = ArrayAdapter(
                        this@MainActivity,
                        android.R.layout.simple_list_item_1,
                        listOf("Ошибка: ${e.message}")
                    )
                }
            }
        }
    }

    private fun parseXml(xml: String): List<String> {
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(StringReader(xml))

        val currencyList = mutableListOf<String>()
        var currentName = ""
        var currentValue = ""

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "Name" -> {
                            parser.next()
                            currentName = parser.text
                        }
                        "Value" -> {
                            parser.next()
                            currentValue = parser.text
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "Valute" && currentName.isNotEmpty() && currentValue.isNotEmpty()) {
                        currencyList.add("$currentName - $currentValue")
                        currentName = ""
                        currentValue = ""
                    }
                }
            }
            parser.next()
        }
        return currencyList
    }
}