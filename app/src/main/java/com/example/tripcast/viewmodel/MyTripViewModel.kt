package com.example.tripcast.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripcast.firebase.TripSaver
import com.example.tripcast.model.Trip
import kotlinx.coroutines.launch

class MyTripViewModel : ViewModel() {
    private val _myTripList = mutableStateListOf<Trip>()
    val myTripList: List<Trip> get() = _myTripList

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

    fun loadTripsFromFirebase() {
        viewModelScope.launch {
            val tripsData = TripSaver.loadTrips()
            android.util.Log.d("MyTripViewModel", "불러온 TripData 개수: ${tripsData.size}")
            tripsData.forEach {
                android.util.Log.d(
                    "MyTripViewModel",
                    "TripData -> destination: ${it.destination}, start: ${it.startDate}, end: ${it.endDate}, weather: ${it.weather.joinToString { w -> w.condition }}"
                )
            }
            val trips = tripsData.map {
                Trip(
                    startDate = it.startDate,
                    endDate = it.endDate,
                    location = it.destination,
                    weather = if (it.weather.isNotEmpty()) it.weather.joinToString { w -> w.condition } else "Unknown",
                    temperature = "",
                    UV = "",
                    airQuality = ""
                )
            }
            _myTripList.clear()
            _myTripList.addAll(trips)
            android.util.Log.d("MyTripViewModel", "변환 후 Trip 개수: ${_myTripList.size}")
        }
    }

}