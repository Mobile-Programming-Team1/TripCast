package com.example.tripcast.model

data class Trip (
    val startDate: String,
    val endDate: String,
    val location: String,
    val weather: String,
    val temperature: String,
    val airQuality: String,
)