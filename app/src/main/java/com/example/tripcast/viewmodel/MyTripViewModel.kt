package com.example.tripcast.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tripcast.MyFirebaseMessagingService
import com.example.tripcast.MyFirebaseMessagingService.Companion.token
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
                airQuality = ""
            )
        )
    }

    fun removeTrip(trip: Trip) {
        MyFirebaseMessagingService.token?.let { token ->
            TripSaver.deleteTripFromFirebase(
                startDate = trip.startDate,
                endDate = trip.endDate,
                location = trip.location,
                userToken = token
            ) { success ->
                if (success) {
                    _myTripList.remove(trip)
                    Log.d("MyTripViewModel", "removeTrip 호출됨: ${trip.location}, ${trip.startDate}")
                } else {
                    android.util.Log.e("MyTripViewModel", "Firebase에서 삭제 실패")
                }
            }
        }
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
                airQuality = airQuality ?: last.airQuality
            )
            _myTripList[_myTripList.lastIndex] = updated
        }
    }

    fun loadTripsFromFirebase(userToken: String) {
        viewModelScope.launch {
            val tripsData = TripSaver.loadTrips(userToken)
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
                    airQuality = "",

                )
            }
            _myTripList.clear()
            _myTripList.addAll(trips)
            android.util.Log.d("MyTripViewModel", "변환 후 Trip 개수: ${_myTripList.size}")
        }
    }
}