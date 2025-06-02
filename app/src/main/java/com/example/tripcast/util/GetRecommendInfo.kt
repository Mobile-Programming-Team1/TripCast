package com.example.tripcast.util

import android.util.Log
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

suspend fun getRecommendInfo(date:String, weather:String): List<String> = withContext(Dispatchers.IO) {
    val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS) // 기본 10s
        .readTimeout(30, TimeUnit.SECONDS)    // 충분히 늘려 줌
        .callTimeout(30, TimeUnit.SECONDS)    // 전체 타임라인
        .build()

    val outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val dateParam  = parsedDate.format(outputFormatter)

    val request = Request.Builder()
        .url("http://10.0.2.2:8000/recommendation?weather=${weather}&date=${dateParam}")
        .build()

    try {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val jsonString = response.body?.string()
            if (jsonString != null) {
                val jsonElement = JsonParser.parseString(jsonString)
                val recommendArray = jsonElement.asJsonObject.getAsJsonArray("list")
                Log.d("recommendArray", recommendArray.toString())
                val result: List<String> = recommendArray.map { it.asString }
                return@withContext result
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    } catch (e: IOException) {
        Log.e("getRecommendInfo", "Network error: ${e.message}", e)
        emptyList()
    }
}