package ru.ilyamorozov.githubsearcher

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import androidx.core.net.toUri

data class AuthorName(
    val login: String,
    val html_url: String
)

data class Repository(
    val name: String,
    val owner: AuthorName,
    val html_url: String,
    val description: String,
    val language: String
)

class RepositoryAdapter(private val context: AppCompatActivity, private val repositories: List<Repository>) :
    ArrayAdapter<Repository>(context, 0, repositories) {

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_repository, parent, false)

        val repo = repositories[position]

        // Устанавливаем значения с учетом пробела
        view.findViewById<TextView>(R.id.repoNameValue).text = repo.name
        view.findViewById<TextView>(R.id.repoAuthorValue).text = repo.owner.login
        view.findViewById<TextView>(R.id.repoLanguageValue).text = repo.language
        view.findViewById<TextView>(R.id.repoDescriptionValue).text = repo.description

        view.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, repo.html_url.toUri())
            context.startActivity(intent)
        }

        return view
    }
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        val searchButton = findViewById<Button>(R.id.searchButton)
        val listView = findViewById<ListView>(R.id.repositoriesListView)

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                fetchRepositories(query, listView)
            } else {
                Toast.makeText(this, getString(R.string.input), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchRepositories(query: String, listView: ListView) {
        Thread {
            try {
                val url = URL("https://api.github.com/search/repositories?q=$query")
                val connection = url.openConnection() as HttpsURLConnection
                connection.connectTimeout = 10000 //

                try {
                    val inputStream = BufferedInputStream(connection.inputStream)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val jsonString = reader.use { it.readText() }

                    runOnUiThread {
                        val repositories = parseJson(jsonString)
                        displayRepositories(repositories, listView)
                    }
                } finally {
                    connection.disconnect()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Ошибка сети: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun parseJson(jsonString: String): List<Repository> {
        val jsonObject = JSONObject(jsonString)
        val itemsArray = jsonObject.getJSONArray("items")
        val repositories = mutableListOf<Repository>()

        for (i in 0 until itemsArray.length()) {
            val item = itemsArray.getJSONObject(i)
            val owner = AuthorName(
                item.getJSONObject("owner").getString("login"),
                item.getJSONObject("owner").getString("html_url")
            )
            val repo = Repository(
                item.getString("name"),
                owner,
                item.getString("html_url"),
                item.getString("description"),
                item.getString("language")
            )
            repositories.add(repo)
        }
        return repositories
    }

    private fun displayRepositories(repositories: List<Repository>, listView: ListView) {
        val adapter = RepositoryAdapter(this, repositories)
        listView.adapter = adapter
    }
}