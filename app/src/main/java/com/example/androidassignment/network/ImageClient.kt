package com.example.androidassignment.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ImageClient {
    private val baseUrl = "https://acharyaprashant.org/api/v2/content/misc/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getService(): ImageService {
        return retrofit.create(ImageService::class.java)
    }
}