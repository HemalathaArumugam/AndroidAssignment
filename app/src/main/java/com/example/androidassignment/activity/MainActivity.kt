package com.example.androidassignment.activity

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidassignment.R
import com.example.androidassignment.adapter.ImageAdapter
import com.example.androidassignment.images.ImageDetails
import com.example.androidassignment.network.ImageClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ImageAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageView: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerview)
        imageView = findViewById(R.id.offline_image)
        setupRecyclerView()

        if (isNetworkAvailable()) {
            fetchImageListFromApi()
        } else {
            imageView.visibility= View.VISIBLE
            imageView.setImageResource(R.drawable.nonetwork)
            Toast.makeText(applicationContext, "Network is not available", Toast.LENGTH_LONG).show()
            Log.e("MainActivity", "Network unavailable")
            // Display a message or dialog to inform the user about no internet connection
        }
    }

    private fun setupRecyclerView() {
        recyclerView.visibility=View.VISIBLE
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        adapter = ImageAdapter(this)
        recyclerView.adapter = adapter
    }

    private fun fetchImageListFromApi() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        Toast.makeText(applicationContext, "Retrieving Data", Toast.LENGTH_SHORT).show()

        val apiService = ImageClient.getService()
        apiService.getImageList().enqueue(object : Callback<List<ImageDetails>> {
            override fun onResponse(
                call: Call<List<ImageDetails>>,
                response: Response<List<ImageDetails>>
            ) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val imageList = response.body()
                    imageList?.let {
                        adapter.setImageList(it) // Update adapter with fetched data
                    }
                } else {
                    Log.e("MainActivity", "Failed to fetch image list: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<ImageDetails>>, t: Throwable) {
                              Log.e("MainActivity", "Network call failed: ${t.message}", t)
            }
        })
    }
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }
}
