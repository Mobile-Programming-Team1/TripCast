package com.example.tripcast.util

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.Manifest
import android.database.Cursor
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.util.*

object CalendarUtil {

    private const val TAG = "CalendarUtil"

    /**
     * 사용 가능한 캘린더 ID를 찾는 함수
     */
    private fun getWritableCalendarId(context: Context): Long? {
        if (!hasCalendarPermission(context)) {
            return null
        }

        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL
        )

        val cursor: Cursor? = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val calendarId = it.getLong(0)
                val displayName = it.getString(1) ?: ""
                val accountName = it.getString(2) ?: ""
                val accessLevel = it.getInt(3)

                Log.d(TAG, "캘린더 발견: ID=$calendarId, 이름=$displayName, 계정=$accountName, 접근레벨=$accessLevel")

                // 쓰기 가능한 캘린더 찾기
                if (accessLevel >= CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR) {
                    Log.d(TAG, "사용 가능한 캘린더 ID: $calendarId")
                    return calendarId
                }
            }
        }

        Log.e(TAG, "사용 가능한 캘린더를 찾을 수 없습니다")
        return null
    }

    /**
     * 캘린더 권한이 있는지 확인
     */
    private fun hasCalendarPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 일정을 캘린더에 등록하는 함수 (개선 버전)
     */
    fun insertEvent(
        context: Context,
        title: String,
        description: String,
        startMillis: Long,
        endMillis: Long
    ) {
        Log.d(TAG, "일정 등록 시작: $title")

        // ✅ 권한 체크
        if (!hasCalendarPermission(context)) {
            Toast.makeText(context, "캘린더 권한이 필요합니다", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "캘린더 권한 없음")
            return
        }

        // ✅ 사용 가능한 캘린더 ID 찾기
        val calendarId = getWritableCalendarId(context)
        if (calendarId == null) {
            Toast.makeText(context, "사용 가능한 캘린더가 없습니다", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "사용 가능한 캘린더 ID 없음")
            return
        }

        Log.d(TAG, "사용할 캘린더 ID: $calendarId")

        // ✅ 이벤트 삽입 정보 구성
        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.TITLE, title)
            put(CalendarContract.Events.DESCRIPTION, description)
            put(CalendarContract.Events.CALENDAR_ID, calendarId) // 동적으로 찾은 ID 사용
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.HAS_ALARM, 1) // 알림 설정
            put(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_DEFAULT)
        }

        try {
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)

            if (uri != null) {
                val eventId = uri.lastPathSegment
                Log.d(TAG, "일정 등록 성공: ID=$eventId")

                // ✅ 알림 추가 (선택사항)
                addEventReminder(context, eventId?.toLongOrNull(), 15) // 15분 전 알림

                Toast.makeText(context, "✅ 일정이 캘린더에 등록되었습니다!", Toast.LENGTH_SHORT).show()
            } else {
                Log.e(TAG, "일정 등록 실패: URI가 null")
                Toast.makeText(context, "❌ 일정 등록 실패", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "일정 등록 중 에러 발생", e)
            Toast.makeText(context, "❌ 일정 등록 실패: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * 일정에 알림을 추가하는 함수
     */
    private fun addEventReminder(context: Context, eventId: Long?, minutes: Int) {
        if (eventId == null) return

        val reminderValues = ContentValues().apply {
            put(CalendarContract.Reminders.EVENT_ID, eventId)
            put(CalendarContract.Reminders.MINUTES, minutes)
            put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
        }

        try {
            val reminderUri = context.contentResolver.insert(
                CalendarContract.Reminders.CONTENT_URI,
                reminderValues
            )

            if (reminderUri != null) {
                Log.d(TAG, "알림 추가 성공: ${minutes}분 전")
            }
        } catch (e: Exception) {
            Log.e(TAG, "알림 추가 실패", e)
        }
    }

    /**
     * 사용 가능한 모든 캘린더 목록을 로그로 출력 (디버깅용)
     */
    fun debugCalendars(context: Context) {
        if (!hasCalendarPermission(context)) {
            Log.e(TAG, "캘린더 권한 없음 - 디버깅 불가")
            return
        }

        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
            CalendarContract.Calendars.OWNER_ACCOUNT
        )

        val cursor: Cursor? = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        Log.d(TAG, "=== 캘린더 목록 디버깅 ===")
        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(0)
                val displayName = it.getString(1) ?: "이름없음"
                val accountName = it.getString(2) ?: "계정없음"
                val accessLevel = it.getInt(3)
                val ownerAccount = it.getString(4) ?: "소유자없음"

                val accessLevelText = when (accessLevel) {
                    CalendarContract.Calendars.CAL_ACCESS_NONE -> "접근불가"
                    CalendarContract.Calendars.CAL_ACCESS_FREEBUSY -> "바쁨여부만"
                    CalendarContract.Calendars.CAL_ACCESS_READ -> "읽기전용"
                    CalendarContract.Calendars.CAL_ACCESS_RESPOND -> "응답가능"
                    CalendarContract.Calendars.CAL_ACCESS_OVERRIDE -> "수정가능"
                    CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR -> "기여가능"
                    CalendarContract.Calendars.CAL_ACCESS_EDITOR -> "편집가능"
                    CalendarContract.Calendars.CAL_ACCESS_OWNER -> "소유자"
                    else -> "알수없음($accessLevel)"
                }

                Log.d(TAG, "캘린더: ID=$id, 이름='$displayName', 계정='$accountName', 접근='$accessLevelText', 소유자='$ownerAccount'")
            }
        }
        Log.d(TAG, "=== 캘린더 목록 끝 ===")
    }
}