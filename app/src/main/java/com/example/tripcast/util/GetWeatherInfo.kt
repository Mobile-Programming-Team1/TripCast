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
suspend fun getWeatherInfo(
    startDate: String,
    endDate: String,
    location: String
): List<WeatherInfo> = withContext(Dispatchers.IO) {
    val weatherList = mutableListOf<WeatherInfo>()

    // 날짜 포맷팅(기존 코드와 동일)
    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val startLocal = LocalDate.parse(startDate, inputFormatter)
    val endLocal   = LocalDate.parse(endDate, inputFormatter)

    val outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val startParam = startLocal.format(outputFormatter)
    val endParam   = endLocal.format(outputFormatter)

    val client = OkHttpClient()
    // URL 끝에 슬래시(/) 반드시 붙이기 (서버가 "/weather/" 로 정의되어 있다고 가정)
    val url = "http://10.0.2.2:8000/weather/" +
            "?city=$location" +
            "&start_date=$startParam" +
            "&end_date=$endParam"

    // 빈 바디(POST 요청 시 body가 필요하면 그대로 사용)
    val emptyBody = "".toRequestBody("application/x-www-form-urlencoded".toMediaTypeOrNull())

    // 여기에 자신의 실제 API 키를 넣으세요
    val apiKey = "afad7c87ebd1b14f5287168defd8d921"

    val request = Request.Builder()
        .url(url)
        // 헤더에 Authorization: Bearer <키> 형태로 추가
        .addHeader("Authorization", "Bearer $apiKey")
        // 만약 서버가 x-api-key 헤더를 사용한다면:
        // .addHeader("x-api-key", apiKey)
        .post(emptyBody)
        .build()

    Log.d("WeatherDebug", "날씨 요청 URL = $url")
    Log.d("WeatherDebug", "Authorization 헤더 = Bearer $apiKey")

    try {
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            val bodyStr = response.body?.string()
            Log.d("WeatherDebug", "서버 응답 본문 = $bodyStr")
            val jsonElement = JsonParser.parseString(bodyStr)
            val forecastArray = jsonElement.asJsonObject.getAsJsonArray("forecast")
            Log.d("forecastArray", forecastArray.toString())

            val daysBetween = ChronoUnit.DAYS.between(startLocal, endLocal).toInt()
            for (i in 0..daysBetween) {
                val date = startLocal.plusDays(i.toLong())
                val dateStr = date.format(inputFormatter)

                val forecastItem = forecastArray[i].asJsonObject
                val weatherStr = forecastItem.get("weather").asString.uppercase()
                Log.d("WeatherParser", "Raw weather string from server: $weatherStr")
                val weatherEnum = try {
                    WeatherType.valueOf(weatherStr)
                } catch (e: IllegalArgumentException) {
                    Log.w("WeatherParser", "Unknown weather type: $weatherStr, defaulting to ETC")
                    WeatherType.ETC
                }

                weatherList.add(WeatherInfo(date = dateStr, weather = weatherEnum))
                Log.d("weatherList", weatherList.toString())
            }
        } else {
            // 401, 403, 404, 500 등 HTTP 에러 코드가 올 때
            Log.e("WeatherDebug", "HTTP Error 코드: ${response.code}")
            val errorBody = response.body?.string()
            Log.e("WeatherDebug", "에러 바디: $errorBody")
        }
    } catch (e: IOException) {
        Log.e("WeatherDebug", "날씨 API 요청 실패", e)
    }

    return@withContext weatherList
}