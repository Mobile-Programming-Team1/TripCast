package com.example.tripcast.firebase

import android.util.Log
import com.example.tripcast.model.WeatherInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

data class DailyWeather(
    val date: String,
    val condition: String
)

data class TripData(
    val destination: String,
    val startDate: String,
    val endDate: String,
    val weather: List<DailyWeather>
)

object TripSaver {
    private val db = FirebaseFirestore.getInstance()

    suspend fun saveTrip(
        destination: String,
        startDate: LocalDate,
        endDate: LocalDate,
        weatherList: List<DailyWeather>
    ) {
        val trip = mapOf(
            "destination" to destination,
            "startDate" to startDate.toString(),
            "endDate" to endDate.toString(),
            "weather" to weatherList
                .sortedBy { it.date } // ✅ 날짜 오름차순 정렬
                .map {
                    mapOf(
                        "date" to it.date,
                        "condition" to it.condition
                    )
                }
        )

        try {
            db.collection("plans").add(trip).await()
            Log.d("TripSaver", "여행 저장 완료")
        } catch (e: Exception) {
            Log.e("TripSaver", "저장 실패", e)
        }


    }

    suspend fun loadTrips(): List<TripData> {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("plans")
                .get()
                .await()
            Log.d("TripSaver", "불러온 trip 도큐먼트 수: ${snapshot.size()}")
            snapshot.documents.mapNotNull { doc ->
                val destination = doc.getString("destination") ?: return@mapNotNull null
                val startDate = doc.getString("startDate") ?: return@mapNotNull null
                val endDate = doc.getString("endDate") ?: return@mapNotNull null
                val weatherList = (doc["weather"] as? List<Map<String, String>>)?.map {
                    DailyWeather(it["date"] ?: "", it["condition"] ?: "")
                } ?: emptyList()

                TripData(destination, startDate, endDate, weatherList)
            }

        } catch (e: Exception) {
            Log.e("TripRepository", "불러오기 실패", e)
            emptyList()
        }
    }

    fun deleteTripFromFirebase(
        startDate: String,
        endDate: String,
        location: String,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection("plans")
            .whereEqualTo("startDate", startDate)
            .whereEqualTo("endDate", endDate)
            .whereEqualTo("destination", location)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("TripSaver", "삭제 시도: ${documents.size()}개 문서 발견")
                for (document in documents) {
                    Log.d("TripSaver", "삭제 대상 문서 ID: ${document.id}")
                    db.collection("plans").document(document.id)
                        .delete()
                        .addOnSuccessListener {
                            Log.d("TripSaver", "여행 삭제 완료")
                            onComplete(true)
                        }
                        .addOnFailureListener { e ->
                            Log.e("TripSaver", "삭제 실패", e)
                            onComplete(false)
                        }
                }
                if (documents.isEmpty) {
                    onComplete(false)
                }
            }
            .addOnFailureListener { e ->
                Log.e("TripSaver", "삭제용 문서 조회 실패", e)
                onComplete(false)
            }
    }
}
