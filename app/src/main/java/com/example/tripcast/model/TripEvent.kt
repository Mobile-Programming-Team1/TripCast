package com.example.tripcast.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

/**
 * 캘린더에 표시할 여행 이벤트 데이터 클래스
 */
data class TripEvent(
    val id: String,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val color: Color = Color(0xFF4CAF50) // 기본 초록색
)