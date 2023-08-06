package com.example.apitesting.ui

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.apitesting.R
import com.example.apitesting.adapter.MovieRecyclerAdapter
import com.example.apitesting.application.MyApplication
import com.example.apitesting.databinding.ActivityMainBinding
import com.example.apitesting.model.Movie
import com.example.apitesting.viewmodel.MainViewModel
import com.example.apitesting.viewmodel.MainViewModelFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var mainViewModel: MainViewModel
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var movieAdapter: MovieRecyclerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //view banding
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        movieAdapter = MovieRecyclerAdapter(this)
        activityMainBinding.recyclerMovie.layoutManager = LinearLayoutManager(this)
        activityMainBinding.recyclerMovie.adapter = movieAdapter

        //initializing repository class using our application class
        val myRepository = (application as MyApplication).recommendRepository
        //initializing our view model using view model factory
        mainViewModel =
            ViewModelProvider(
                this,
                MainViewModelFactory(myRepository)
            )[MainViewModel::class.java]
        GlobalScope.launch {
            mainViewModel.getMovies()
        }

        mainViewModel.movieResponse.observe(this, Observer {
            val movieResponse = it.body()
            val movieList = mutableListOf<String>()
            if (movieResponse != null) {
                for (movie in movieResponse) {
                    movieList.add(movie)
                }
            }
            val movieAdapter = ArrayAdapter(this, R.layout.list_item, movieList)
            activityMainBinding.atMovieName.setAdapter(movieAdapter)

        })
        activityMainBinding.btnRecommend.setOnClickListener {
            val movieName = activityMainBinding.atMovieName.text.toString()
            GlobalScope.launch {
                Log.d("KRISHNA", movieName)
                mainViewModel.recommendMovie(movieName)
            }
        }
        mainViewModel.recommendedMovieResponse.observe(this, Observer {
            var movieList = mutableListOf<Movie>()
            val movieName = mutableListOf<String>()
            val movieUrl = mutableListOf<String>()
            val recommendedMovie = it.body()
            if (recommendedMovie != null) {
                for ((count, movie) in recommendedMovie.withIndex()) {
                    if (count == 0) {
                        for (names in movie) {
                            movieName.add(names)
                        }
                    } else {
                        for (names in movie) {
                            movieUrl.add(names)
                        }
                    }
                }
            }
            val size = movieName.size
            var idx = 0
            while (idx < size) {
                var movie = Movie(movieName[idx], movieUrl[idx])
                movieList.add(movie)
                idx++
            }
            for(movie in movieList){
                Log.d("KRISHNA", movie.toString())
            }
            movieAdapter.submitList(movieList)
        })

    }
}