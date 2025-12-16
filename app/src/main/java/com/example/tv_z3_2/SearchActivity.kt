package com.example.tv_z3_2

import Movie
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class SearchActivity : AppCompatActivity() {

    private val apiKey = "900abc80"
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var adapter: MovieAdapter
    private lateinit var progressBar: ProgressBar

    // Храним все фильмы
    private val allMovies = ArrayList<Movie>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_activity)

        recyclerView = findViewById(R.id.recycler)
        searchView = findViewById(R.id.search)
        progressBar = findViewById(R.id.progress)

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = MovieAdapter { movie ->
            openMovieDetails(movie)
        }
        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchMovies(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (it.length >= 3) {
                        searchMovies(it)
                    } else if (it.isEmpty()) {
                        adapter.data = allMovies
                    }
                }
                return false
            }
        })

        loadInitialMovies()
    }

    private fun loadInitialMovies() {
        searchMovies("movie")
    }

    private fun searchMovies(query: String) {
        if (query.isEmpty()) return

        progressBar.visibility = View.VISIBLE

        val url = "https://www.omdbapi.com/?s=$query&apikey=$apiKey"

        val queue = Volley.newRequestQueue(this)

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                try {
                    val obj = JSONObject(response)
                    if (obj.getString("Response") == "True") {
                        val moviesArray = obj.getJSONArray("Search")
                        val tempMovies = ArrayList<Movie>()
                        for (i in 0 until moviesArray.length()) {
                            val movieJson = moviesArray.getJSONObject(i)
                            val imdbID = movieJson.getString("imdbID")
                            loadMovieDetails(imdbID) { movie ->
                                movie?.let {
                                    tempMovies.add(it)
                                    if (tempMovies.size == moviesArray.length()) {
                                        allMovies.clear()
                                        allMovies.addAll(tempMovies)
                                        adapter.data = tempMovies
                                        progressBar.visibility = View.GONE
                                    }
                                }
                            }
                        }
                        if (moviesArray.length() == 0) {
                            adapter.data = emptyList()
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, "Фильмы не найдены", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        adapter.data = emptyList()
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Фильмы не найдены", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Ошибка парсинга", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Ошибка сети: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(stringRequest)
    }

    private fun loadMovieDetails(imdbID: String, callback: (Movie?) -> Unit) {
        val url = "https://www.omdbapi.com/?apikey=$apiKey&i=$imdbID"

        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                try {
                    val movieObj = JSONObject(response)

                    val genresString = movieObj.getString("Genre")
                    val genresList = if (genresString != "N/A") {
                        genresString.split(", ").toList()
                    } else {
                        listOf("Не указано")
                    }

                    val movie = Movie(
                        Title = movieObj.getString("Title"),
                        Poster = movieObj.getString("Poster"),
                        Genre = genresList,
                        Plot = movieObj.getString("Plot"),
                        Year = movieObj.getString("Year"),
                        Runtime = movieObj.getString("Runtime"),
                        imdbID = imdbID
                    )

                    callback(movie)
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback(null)
                }
            },
            { error ->
                callback(null)
            }
        )

        queue.add(stringRequest)
    }

    private fun openMovieDetails(movie: Movie) {
        val fragment = InfoMovieFragment().apply {
            arguments = Bundle().apply {
                putString("title", movie.Title)
                putString("poster", movie.Poster)
                putStringArrayList("genres", ArrayList(movie.Genre))
                putString("plot", movie.Plot)
                putString("year", movie.Year)
                putString("runtime", movie.Runtime)
            }
        }

        // Скрываем RecyclerView и SearchView, показываем контейнер
        findViewById<View>(R.id.recycler)?.visibility = View.GONE
        findViewById<View>(R.id.search)?.visibility = View.GONE
        findViewById<View>(R.id.fragment_container)?.visibility = View.VISIBLE

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // чтобы можно было вернуться назад
            .commit()
    }


}