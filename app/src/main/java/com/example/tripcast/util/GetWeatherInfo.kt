package com.example.tripcast.util

import android.util.Log
import com.example.tripcast.model.WeatherInfo
import com.example.tripcast.model.WeatherType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun getWeatherInfo(startDate:String, endDate:String, location:String): List<WeatherInfo> {
    val weatherList = mutableListOf<WeatherInfo>()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val start = LocalDate.parse(startDate, formatter)
    val end = LocalDate.parse(endDate, formatter)

    val daysBetween = ChronoUnit.DAYS.between(start, end).toInt()

    for (i in 0..daysBetween) {
        val date = start.plusDays(i.toLong())
        val dateStr = date.format(formatter)
        weatherList.add(WeatherInfo(date = dateStr, weather = WeatherType.values().random()))
    }
    return weatherList
}