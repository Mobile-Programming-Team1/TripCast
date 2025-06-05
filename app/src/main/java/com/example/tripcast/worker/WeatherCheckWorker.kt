// 파일: WeatherCheckWorker.kt
package com.example.tripcast.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

class WeatherCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val TAG = "WeatherCheckWorker"

    // 테스트용 서버 키 (실서비스에서는 절대 클라이언트에 노출 금지)
    private val FCM_SERVER_KEY = "ad4111893032f59d996b5c9bd8f22b4905915f98"

    private val httpClient = OkHttpClient()
    private val gson = Gson()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔍 날씨 비교 시작: ${Date()}")

            // Firestore 인스턴스 가져오기
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val plansSnapshot = firestore.collection("plans").get().await()

            // 수동으로 사용할 FCM 토큰을 여기서 지정하세요.
            val fcmToken = "d9HFDFKWRcWmgz8x7KrxrJ:APA91bEJ_lp6_5Eec95xx9kQGm1AredF20vXScSG8StvFbs4bWi12OlDkKFiZB3Fltd42oPFUASuPMXND5DNunGcUQjzO6qrkmNs6zj5Rnq5SRP4__nQk_s"

            for (planDoc in plansSnapshot.documents) {
                // planDoc 데이터가 없으면 건너뛰기
                val data = planDoc.data ?: continue
                val destination = data["destination"] as? String ?: continue

                // destination 변수 선언: 이 줄이 있어야 아래에서 destination 을 사용할 수 있습니다.

                @Suppress("UNCHECKED_CAST")
                val weatherList = data["weather"] as? List<Map<String, String>> ?: emptyList()

                // 예상 날씨 리스트 만들기
                val expectedList = weatherList.mapNotNull { item ->
                    val dateStr = item["date"]
                    val cond = item["condition"]
                    if (dateStr != null && cond != null) {
                        dateStr to cond.uppercase(Locale.getDefault())
                    } else {
                        null
                    }
                }

                for ((dateStr, expectedCond) in expectedList) {
                    // API 호출 제한 회피용 딜레이
                    delay(1100)

                    // 실제 날씨 가져오기
                    val actualCond = getRealWeather(destination, dateStr)

                    if (!expectedCond.equals(actualCond, ignoreCase = true)) {
                        Log.d(
                            TAG,
                            "❗ 차이 발생: $destination | $dateStr | 예상: $expectedCond, 실제: $actualCond"
                        )
                        val title = "$dateStr 날씨 변경"
                        val body = "${destination}의 날씨가 예상($expectedCond)과 달라요! 실제: $actualCond"
                        sendFcmViaHttpV1(fcmToken, title, body)
                    } else {
                        Log.d(TAG, "✅ 일치: $destination | $dateStr | $expectedCond")
                    }
                }
            }

            Log.d(TAG, "✅ 날씨 비교 완료: ${Date()}")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "⚠️ WeatherCheckWorker 실패", e)
            Result.retry()
        }
    }

    /**
     * 외부 날씨 API 호출 함수.
     * 예: localhost:8000/weather/?city=Seoul&start_date=20250605&end_date=20250605
     */
    private fun getRealWeather(city: String, dateStr: String): String {
        return try {
            val baseUrl = "http://10.0.2.2:8000/weather/"
            val noHyphen = dateStr.replace("-", "")
            val url = "$baseUrl?city=${city}&start_date=$noHyphen&end_date=$noHyphen"

            val request = Request.Builder()
                .url(url)
                .post("".toRequestBody()) // POST 바디가 필요 없으면 빈 문자열 사용
                .build()

            httpClient.newCall(request).execute().use { resp ->
                if (resp.isSuccessful) {
                    val bodyStr = resp.body?.string() ?: return "UNKNOWN"
                    val jsonObj = gson.fromJson(bodyStr, Map::class.java)
                    val forecast = (jsonObj["forecast"] as? List<Map<String, Any>>)?.firstOrNull()
                    val weatherStr = forecast?.get("weather") as? String
                    weatherStr?.uppercase(Locale.getDefault()) ?: "UNKNOWN"
                } else {
                    Log.w(TAG, "Weather API 에러: ${resp.code}")
                    "UNKNOWN"
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, "getRealWeather 예외", ex)
            "UNKNOWN"
        }
    }

    /**
     * FCM HTTP v1 REST API로 data 메시지 전송.
     * (테스트용: 서버 키를 클라이언트에 직접 넣음)
     */
    private fun sendFcmViaHttpV1(token: String, title: String, body: String) {
        // Debug log: print the raw server key
        Log.d(TAG, "DEBUG: FCM_SERVER_KEY='$FCM_SERVER_KEY'")
        val url = "https://fcm.googleapis.com/fcm/send"
        val jsonObj = mapOf(
            "to" to token,
            "data" to mapOf(
                "title" to title,
                "body" to body
            )
        )
        val jsonPayload = gson.toJson(jsonObj)
        // Debug log: print the JSON payload
        Log.d(TAG, "DEBUG: FCM payload='$jsonPayload'")
        val requestBody = jsonPayload.toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "key=$FCM_SERVER_KEY")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()
        // Debug log: print the Authorization header value
        Log.d(TAG, "DEBUG: Authorization header='key=$FCM_SERVER_KEY'")

        try {
            httpClient.newCall(request).execute().use { resp ->
                if (resp.isSuccessful) {
                    Log.d(TAG, "📬 FCM 전송 성공: $title → $token")
                } else {
                    val respBody = resp.body?.string()
                    Log.w(TAG, "⚠️ FCM 전송 실패: code=${resp.code}, body=$respBody")
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, "⚠️ sendFcmViaHttpV1 예외", ex)
        }
    }
}