package com.example.tripcast.util

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.Manifest
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.util.*

object CalendarUtil { //일정 삽입 클래스

    fun insertEvent(
        context: Context,
        title: String,
        description: String,
        startMillis: Long,
        endMillis: Long
    ) {
        // ✅ 권한 체크
        if (
            ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "캘린더 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ 이벤트 삽입 정보 구성
        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.CALENDAR_ID, 1) // 기본 캘린더 ID
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }

        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)

        if (uri != null) {
            Toast.makeText(context, "일정이 캘린더에 등록되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "일정 등록 실패", Toast.LENGTH_SHORT).show()
        }
    }
}