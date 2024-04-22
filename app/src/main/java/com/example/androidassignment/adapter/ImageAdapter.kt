package com.example.androidassignment.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.util.Log
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidassignment.R
import com.example.androidassignment.images.ImageDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class ImageAdapter(private val context: Context) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private var imageList: List<ImageDetails> = emptyList()
    private val memoryCache: LruCache<String, Bitmap>

    init {
        // Initialize memory cache (1/8th of max memory)
        Log.i("memorycache-->","starts")
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_list, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(imageList[position])
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setImageList(list: List<ImageDetails>) {
        imageList = list
        notifyDataSetChanged()
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.grid_image)

        fun bind(imageDetails: ImageDetails) {
            val imageUrl = "${imageDetails.thumbnail?.domain}/${imageDetails.thumbnail?.basePath}/0/${imageDetails.thumbnail?.key}"
            loadImage(imageUrl, imageView)
        }
    }

    private fun loadImage(imageUrl: String, imageView: ImageView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var bitmap: Bitmap? = null

                // Check if image is available in memory cache
                synchronized(memoryCache) {
                    Log.i("-->","getmemory")
                    bitmap = memoryCache.get(imageUrl)
                }

                if (bitmap == null) {
                    // Image not found in memory cache, try loading from disk cache
                    Log.i("-->","diskmemory")
                    bitmap = loadBitmapFromDisk(imageUrl)
                }

                if (bitmap == null) {
                    // Image not found in memory or disk cache, download from network
                    if (isNetworkAvailable()) {
                        bitmap = downloadBitmap(imageUrl)

                        bitmap?.let {
                            // Save downloaded bitmap to disk cache and memory cache
                            saveBitmapToDisk(it, imageUrl)
                            synchronized(memoryCache) {
                                Log.i("-->","putmemory")
                                memoryCache.put(imageUrl, it)
                            }
                        }
                    }
                    else{

                        bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_placeholder)
                        bitmap = loadBitmapFromDisk(imageUrl)
                    }
                }

                // Display bitmap on the main thread
                withContext(Dispatchers.Main) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap)
                    } else {
                        // Display placeholder if bitmap is still null
                        imageView.setImageResource(R.drawable.ic_placeholder)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error (e.g., display placeholder image)
                withContext(Dispatchers.Main) {
                    imageView.setImageResource(R.drawable.ic_placeholder)
                }
            }
        }
    }


    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }

    private fun loadBitmapFromDisk(imageUrl: String): Bitmap? {
        val fileName = getFileNameFromUrl(imageUrl)
        val file = File(context.filesDir, fileName)
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.RGB_565 // Use RGB_565 for reduced memory usage
        options.inSampleSize = 2 // Sample down the bitmap by a factor of 2 to reduce memory usage

        return try {
            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun saveBitmapToDisk(bitmap: Bitmap, imageUrl: String) {
        val fileName = getFileNameFromUrl(imageUrl)
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    private fun downloadBitmap(imageUrl: String): Bitmap? {
        var inputStream: InputStream? = null
        var bitmap: Bitmap? = null
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            inputStream = connection.inputStream
            bitmap = BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return bitmap
    }

    private fun getFileNameFromUrl(url: String): String {
        return url.substring(url.lastIndexOf('/') + 1)
    }
}
