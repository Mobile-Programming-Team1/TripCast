package com.example.tripcast.util

import android.util.Log
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

suspend fun getAvailableLocation():List<String> = withContext(Dispatchers.IO){
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("http://10.0.2.2:8000/cities")
        .build()

    try {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val jsonString = response.body?.string()
            if (jsonString != null) {
                val jsonElement = JsonParser.parseString(jsonString)
                val citiesArray = jsonElement.asJsonObject.getAsJsonArray("cities")
                Log.d("citiesArray", citiesArray.toString())
                citiesArray.map { it.asJsonObject.get("name").asString }
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    } catch (e: IOException) {
        Log.e("getAvailableLocation", "Network error: ${e.message}", e)
        emptyList()
    }
}