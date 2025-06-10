package com.example.tripcast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tripcast.model.Trip
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TripItem(
    trip: Trip,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 위치 아이콘
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 여행 정보
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 여행지
                Text(
                    text = trip.location,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 날짜
                Text(
                    text = formatDateRange(trip.startDate, trip.endDate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 날씨 정보
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 날씨 상태
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = getWeatherEmoji(trip.weather),
                            fontSize = 16.sp
                        )
                        Text(
                            text = trip.weather,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 온도
                    if (trip.temperature.isNotBlank()) {
                        Text(
                            text = "${trip.temperature}°C",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 대기질 표시
            if (trip.airQuality.isNotBlank()) {
                AirQualityChip(airQuality = trip.airQuality)
            }
        }
    }
}

// 날짜 범위 포맷팅 함수
private fun formatDateRange(startDate: String, endDate: String): String {
    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val start = LocalDate.parse(startDate, formatter)
        val end = LocalDate.parse(endDate, formatter)

        val displayFormatter = DateTimeFormatter.ofPattern("M월 d일")

        if (start.isEqual(end)) {
            start.format(displayFormatter)
        } else {
            "${start.format(displayFormatter)} ~ ${end.format(displayFormatter)}"
        }
    } catch (e: Exception) {
        "$startDate ~ $endDate"
    }
}

// 날씨에 따른 이모지 반환
private fun getWeatherEmoji(weather: String): String {
    return when (weather.lowercase()) {
        "clear", "sunny" -> "☀️"
        "clouds", "cloudy" -> "☁️"
        "rain", "rainy" -> "🌧️"
        "snow", "snowy" -> "❄️"
        "thunderstorm" -> "⛈️"
        "drizzle" -> "🌦️"
        "mist", "fog" -> "🌫️"
        "unknown" -> "❓"
        else -> "🌤️"
    }
}

// 대기질 칩 컴포넌트
@Composable
private fun AirQualityChip(airQuality: String) {
    val (color, text) = when (airQuality.lowercase()) {
        "good" -> Color(0xFF4CAF50) to "좋음"
        "moderate" -> Color(0xFFFF9800) to "보통"
        "poor", "bad" -> Color(0xFFF44336) to "나쁨"
        "very poor" -> Color(0xFF9C27B0) to "매우 나쁨"
        else -> Color(0xFF607D8B) to airQuality
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview
@Composable
private fun TripItemPreview() {
    val sampleTrip = Trip(
        startDate = "2025-06-15",
        endDate = "2025-06-18",
        location = "Tokyo, Japan",
        weather = "Clear",
        temperature = "22",
        airQuality = "Good"
    )

    TripItem(trip = sampleTrip)
}