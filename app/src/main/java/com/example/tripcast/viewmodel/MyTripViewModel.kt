package com.example.tripcast.viewmodel

import androidx.lifecycle.ViewModel
import com.example.tripcast.model.Trip

class MyTripViewModel :ViewModel() {
    private var _myTripList = mutableListOf<Trip>()
    var myTripList: List<Trip> = _myTripList

    fun addEmptyTrip(location: String) {
        _myTripList.add(
            Trip(
                startDate = "",
                endDate = "",
                location = location,
                weather = "",
                temperature = "",
                UV = "",
                airQuality = ""
            )
        )
    }

    fun updateLastTrip(
        startDate: String? = null,
        endDate: String? = null,
        weather: String? = null,
        temperature: String? = null,
        UV: String? = null,
        airQuality: String? = null
    ) {
        if (_myTripList.isNotEmpty()) {
            val last = _myTripList.last()
            val updated = last.copy(
                startDate = startDate ?: last.startDate,
                endDate = endDate ?: last.endDate,
                weather = weather ?: last.weather,
                temperature = temperature ?: last.temperature,
                UV = UV ?: last.UV,
                airQuality = airQuality ?: last.airQuality
            )
            _myTripList[_myTripList.lastIndex] = updated
        }
    }
}