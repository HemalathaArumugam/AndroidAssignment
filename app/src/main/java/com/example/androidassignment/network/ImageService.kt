package com.example.androidassignment.network

import com.example.androidassignment.images.ImageDetails
import retrofit2.Call
import retrofit2.http.GET

interface ImageService {
    @GET("media-coverages?limit=100")
    fun getImageList(): Call<List<ImageDetails>>

}