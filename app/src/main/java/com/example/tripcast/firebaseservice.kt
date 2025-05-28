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

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "FCM Token: $token")

        // Firestore에 저장하려면 사용자 ID와 함께 저장 (예: plans 컬렉션에)
        val db = FirebaseFirestore.getInstance()
        val uid = "user123"  // 실제 로그인된 사용자 uid로 교체 필요

        val updates = mapOf("token" to token)

        db.collection("plans")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (doc in querySnapshot.documents) {
                    db.collection("plans").document(doc.id).update(updates)
                }
            }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: "TripCast 알림"
        val body = message.notification?.body ?: "새로운 알림이 도착했습니다."

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
        fun fetchAndLogToken() {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d("FCM", "🔥 수동으로 받아온 FCM Token: $token")
                } else {
                    Log.w("FCM", "FCM 토큰 가져오기 실패", task.exception)
                }
            }
        }
    }
}