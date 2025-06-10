package com.example.tripcast.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tripcast.model.TripEvent
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalendarView(
    initialDate: LocalDate = LocalDate.now(),
    tripEvents: List<TripEvent> = emptyList(), // 여행 일정 리스트
    onDateSelected: (LocalDate) -> Unit = {}
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }
    var selectedDate by remember { mutableStateOf(initialDate) }

    Column(modifier = Modifier.fillMaxWidth()) {
        CalendarHeader(
            currentMonth = currentMonth,
            onPreviousClick = { currentMonth = currentMonth.minusMonths(1) },
            onNextClick = { currentMonth = currentMonth.plusMonths(1) }
        )

        DaysOfWeekHeader()

        CalendarDays(
            currentMonth = currentMonth,
            selectedDate = selectedDate,
            tripEvents = tripEvents,
            onDateSelected = { date ->
                selectedDate = date
                onDateSelected(date)
            }
        )

        // 여행 범례 표시
        if (tripEvents.isNotEmpty()) {
            TripLegend(tripEvents = tripEvents)
        }
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: YearMonth,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous Month"
            )
        }

        Text(
            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            style = MaterialTheme.typography.titleMedium
        )

        IconButton(onClick = onNextClick) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Next Month"
            )
        }
    }
}

@Composable
private fun DaysOfWeekHeader() {
    Row(modifier = Modifier.fillMaxWidth()) {
        val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

        daysOfWeek.forEach { day ->
            Text(
                text = day,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// 날짜가 여행 기간에 포함되는지 확인
private fun getTripForDate(date: LocalDate, tripEvents: List<TripEvent>): TripEvent? {
    return tripEvents.find { trip ->
        !date.isBefore(trip.startDate) && !date.isAfter(trip.endDate)
    }
}

// 여행 날짜의 위치를 판단 (시작일/중간/종료일)
private fun getTripDateType(date: LocalDate, trip: TripEvent): TripDateType {
    return when {
        date.isEqual(trip.startDate) && date.isEqual(trip.endDate) -> TripDateType.SINGLE_DAY
        date.isEqual(trip.startDate) -> TripDateType.START
        date.isEqual(trip.endDate) -> TripDateType.END
        else -> TripDateType.MIDDLE
    }
}

private enum class TripDateType {
    START, MIDDLE, END, SINGLE_DAY
}

@Composable
private fun CalendarDays(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    tripEvents: List<TripEvent>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val lastDay = currentMonth.atEndOfMonth().dayOfMonth

    Column(modifier = Modifier.fillMaxWidth()) {
        var dayCounter = 1

        for (i in 0 until 6) {
            if (dayCounter > lastDay) break

            Row(modifier = Modifier.fillMaxWidth()) {
                for (j in 0 until 7) {
                    if (i == 0 && j < firstDayOfWeek || dayCounter > lastDay) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    } else {
                        val date = currentMonth.atDay(dayCounter)
                        val isSelected = date.equals(selectedDate)
                        val trip = getTripForDate(date, tripEvents)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // 여행 배경 표시
                            if (trip != null) {
                                val dateType = getTripDateType(date, trip)
                                TripBackground(
                                    trip = trip,
                                    dateType = dateType,
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            // 날짜 원형 버튼
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            trip != null -> trip.color.copy(alpha = 0.3f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = if (trip != null) 2.dp else 0.dp,
                                        color = trip?.color ?: Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { onDateSelected(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayCounter.toString(),
                                    color = when {
                                        isSelected -> Color.White
                                        trip != null -> trip.color
                                        else -> MaterialTheme.colorScheme.onBackground
                                    },
                                    fontWeight = if (trip != null) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        dayCounter++
                    }
                }
            }
        }
    }
}

// 여행 기간 배경을 그리는 컴포넌트
@Composable
private fun TripBackground(
    trip: TripEvent,
    dateType: TripDateType,
    modifier: Modifier = Modifier
) {
    val shape = when (dateType) {
        TripDateType.START -> RoundedCornerShape(
            topStart = 20.dp, bottomStart = 20.dp,
            topEnd = 4.dp, bottomEnd = 4.dp
        )
        TripDateType.END -> RoundedCornerShape(
            topStart = 4.dp, bottomStart = 4.dp,
            topEnd = 20.dp, bottomEnd = 20.dp
        )
        TripDateType.MIDDLE -> RoundedCornerShape(4.dp)
        TripDateType.SINGLE_DAY -> RoundedCornerShape(20.dp)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(trip.color.copy(alpha = 0.2f))
    )
}

// 여행 범례 컴포넌트
@Composable
private fun TripLegend(tripEvents: List<TripEvent>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "📅 여행 일정",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        tripEvents.forEach { trip ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(trip.color)
                )

                Text(
                    text = "${trip.title} (${trip.startDate.format(DateTimeFormatter.ofPattern("M/d"))} ~ ${trip.endDate.format(DateTimeFormatter.ofPattern("M/d"))})",
                    modifier = Modifier.padding(start = 12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview
@Composable
private fun CalendarViewWithTripsPreview() {
    val sampleTrips = listOf(
        TripEvent(
            id = "1",
            title = "도쿄 여행",
            startDate = LocalDate.now().plusDays(3),
            endDate = LocalDate.now().plusDays(7),
            color = Color(0xFF2196F3)
        ),
        TripEvent(
            id = "2",
            title = "부산 여행",
            startDate = LocalDate.now().plusDays(15),
            endDate = LocalDate.now().plusDays(17),
            color = Color(0xFFFF9800)
        )
    )

    CalendarView(tripEvents = sampleTrips)
}