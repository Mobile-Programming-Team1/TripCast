package com.example.tripcast.util

import android.util.Log
import com.example.tripcast.model.WeatherInfo
import com.example.tripcast.model.WeatherType
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

suspend fun getWeatherInfo(startDate:String, endDate:String, location:String): List<WeatherInfo> = withContext(Dispatchers.IO) {
    val weatherList = mutableListOf<WeatherInfo>()

    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // 애플리케이션 UI용 포맷터
    val startLocal = LocalDate.parse(startDate, inputFormatter)
    val endLocal = LocalDate.parse(endDate, inputFormatter)

    val outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd") // api 인자용 포맷터
    val startParam = startLocal.format(outputFormatter)
    val endParam   = endLocal.format(outputFormatter)

    val client = OkHttpClient()
    val url = "http://3.107.21.54:8000/weather" +
            "?city=${location}" +
            "&start_date=${startParam}" +
            "&end_date=${endParam}"
    val emptyBody = "".toRequestBody("application/x-www-form-urlencoded".toMediaTypeOrNull())
    val request = Request.Builder()
        .url(url)
        .post(emptyBody)
        .build()

    try {
        val response = client.newCall(request).execute()
        if(response.isSuccessful) {
            val responseBody = response.body    // ← 그냥 Java 메서드 이름
            val jsonString = responseBody?.string()
//            val jsonString = response.body?.string()
            val jsonElement = JsonParser.parseString(jsonString)
            val forecastArray = jsonElement.asJsonObject.getAsJsonArray("forecast")
            Log.d("forecastArray", forecastArray.toString())

            val daysBetween = ChronoUnit.DAYS.between(startLocal, endLocal).toInt()

            for (i in 0..daysBetween) {
                val date = startLocal.plusDays(i.toLong())
                val dateStr = date.format(inputFormatter)

                val forecastItem = forecastArray[i].asJsonObject

                val weatherStr = forecastItem.get("weather").asString.uppercase()  // ← 여기서 소문자 → 대문자 변환

                val weatherEnum = try {
                    WeatherType.valueOf(weatherStr)
                } catch (e: IllegalArgumentException) {
                    Log.w("WeatherParser", "Unknown weather type: $weatherStr, defaulting to CLEAR")
                    WeatherType.ETC
                }

                weatherList.add(WeatherInfo(date = dateStr, weather = weatherEnum))
                Log.d("weatherList", weatherList.toString())
            }
        }
    } catch(e: IOException) {
        Log.e("getWeatherInfo", "Network error: ${e.message}", e)
    }

    return@withContext weatherList
}