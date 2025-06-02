package com.example.tripcast.util

import android.util.Log
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

suspend fun getAirInfo(startDate:String, endDate:String, location:String) = withContext(Dispatchers.IO){
    val airStateList = mutableListOf<Int>()

    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // 애플리케이션 UI용 포맷터
    val startLocal = LocalDate.parse(startDate, inputFormatter)
    val endLocal = LocalDate.parse(endDate, inputFormatter)

    val outputFormatter = DateTimeFormatter.ofPattern("yyyyMMdd") // api 인자용 포맷터
    val startParam = startLocal.format(outputFormatter)
    val endParam   = endLocal.format(outputFormatter)

    val client = OkHttpClient()
    val url = "http://10.0.2.2:8000/air" +
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
            val responseBody = response.body
            val jsonString = responseBody?.string()
//            val jsonString = response.body?.string()
            val jsonElement = JsonParser.parseString(jsonString)
            val airArray = jsonElement.asJsonObject.getAsJsonArray("list")
            Log.d("airArray", airArray.toString())

            val daysBetween = ChronoUnit.DAYS.between(startLocal, endLocal).toInt()

            for (i in 0..daysBetween) {
                val date = startLocal.plusDays(i.toLong())
                val dateStr = date.format(inputFormatter)

                val airItem = airArray[i].asJsonObject
                val airValue = airItem.get("air").asInt

                airStateList.add(airValue)
            }
        }
    } catch(e: IOException) {
        Log.e("getAirInfo", "Network error: ${e.message}", e)
    }

    Log.d("airStateList", airStateList.toString())


    return@withContext airStateList
}