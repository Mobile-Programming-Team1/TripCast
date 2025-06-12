package com.example.tripcast.firebase

import android.util.Log
import com.example.tripcast.model.WeatherInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

data class DailyWeather(
    val date: String,
    val condition: String,
    val fineDust: Int
)

data class TripData(
    val destination: String,
    val startDate: String,
    val endDate: String,
    val weather: List<DailyWeather>
)

object TripSaver {
    private val db = FirebaseFirestore.getInstance()
    //여기수정
    suspend fun saveTrip(
        destination: String,
        startDate: LocalDate,
        endDate: LocalDate,
        weatherList: List<DailyWeather>,
        userToken: String
    ) {
        val sortedWeather = weatherList.sortedBy { it.date }
        val trip = mapOf(
            "destination" to destination,
            "startDate" to startDate.toString(),
            "endDate" to endDate.toString(),
            "weather" to sortedWeather
                .mapIndexed { index, it ->
                    mapOf(
                        "date" to it.date,
                        "condition" to it.condition,
                        "fineDust" to it.fineDust
                    ) //여기수정
                }
        )

        try {
            db.collection("users") //여기 수정
                .document(userToken)
                .collection("trips")
                .add(trip)
                .await()
            Log.d("TripSaver", "여행 저장 완료")
        } catch (e: Exception) {
            Log.e("TripSaver", "저장 실패", e)
        }


    }

    suspend fun loadTrips(userToken: String): List<TripData> {
        return try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("users")
                .document(userToken)
                .collection("trips")
                .get()
                .await()

            Log.d("TripSaver", "불러온 trip 도큐먼트 수: ${snapshot.size()}")

            snapshot.documents.mapNotNull { doc ->
                val destination = doc.getString("destination") ?: return@mapNotNull null
                val startDate = doc.getString("startDate") ?: return@mapNotNull null
                val endDate = doc.getString("endDate") ?: return@mapNotNull null
                val weatherList = (doc["weather"] as? List<Map<String, Any?>>)?.map {
                    DailyWeather(
                        it["date"] as? String ?: "",
                        it["condition"] as? String ?: "",
                        (it["fineDust"] as? Long)?.toInt() ?: 0
                    )
                } ?: emptyList()

                TripData(destination, startDate, endDate, weatherList)
            }
        } catch (e: Exception) {
            Log.e("TripSaver", "불러오기 실패", e)
            emptyList()
        }
    }


    fun deleteTripFromFirebase(
        startDate: String,
        endDate: String,
        location: String,
        userToken: String,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection("users")
            .document(userToken)
            .collection("trips")
            .whereEqualTo("startDate", startDate)
            .whereEqualTo("endDate", endDate)
            .whereEqualTo("destination", location)
            .get()
            .addOnSuccessListener { documents ->
                val firstDoc = documents.firstOrNull()
                if (firstDoc != null) {
                    db.collection("users")
                        .document(userToken)
                        .collection("trips")
                        .document(firstDoc.id)
                        .delete()
                        .addOnSuccessListener {
                            onComplete(true)
                        }
                        .addOnFailureListener {
                            onComplete(false)
                        }
                } else {
                    onComplete(false)
                }
                if (documents.isEmpty) onComplete(false)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }
}
