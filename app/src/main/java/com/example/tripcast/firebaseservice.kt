package com.example.tripcast  // ← 실제 패키지 이름으로 바꿔야 함

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Random
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.FirebaseFirestore

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(newToken: String) {
        token = newToken
        Log.d("FCM", "FCM Token: $newToken")
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(newToken).set(mapOf("token" to newToken))
    }
    //바꿔야겠다
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "TripCast 알림"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: "새로운 알림이 도착했습니다."

        val channelId = "tripcast_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "TripCast 알림",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setGroup(null)
            .setGroupSummary(false)

        notificationManager.notify(Random().nextInt(), builder.build())
    }

    companion object {
        var token: String? = null

        fun fetchAndLogToken() {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val resultToken = task.result
                    token = resultToken
                    Log.d("FCM", "🔥 수동으로 받아온 FCM Token: $resultToken")

                    resultToken?.let {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(it).set(mapOf("token" to it))
                    }
                } else {
                    Log.w("FCM", "FCM 토큰 가져오기 실패", task.exception)
                }
            }
        }
    }
}