package com.example.tripcast.ui.screens

import android.Manifest
import android.content.Intent
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tripcast.model.TripEvent
import com.example.tripcast.ui.components.CalendarView
import com.example.tripcast.ui.components.TripItem
import com.example.tripcast.viewmodel.MyTripViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    onNavigateToSearch: () -> Unit,
    viewModel: MyTripViewModel,
    onNavigateToPrev: () -> Unit
) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // 캘린더 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val writeCalendarGranted = permissions[Manifest.permission.WRITE_CALENDAR] ?: false
        val readCalendarGranted = permissions[Manifest.permission.READ_CALENDAR] ?: false

        if (writeCalendarGranted && readCalendarGranted) {
            // 권한이 승인되면 현재 선택된 날짜의 여행들을 실시간으로 계산
            val currentTripsOnSelectedDate = viewModel.myTripList.filter { trip ->
                try {
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val start = LocalDate.parse(trip.startDate, formatter)
                    val end = LocalDate.parse(trip.endDate, formatter)
                    !selectedDate.isBefore(start) && !selectedDate.isAfter(end)
                } catch (e: Exception) {
                    false
                }
            }
            registerToCalendar(context, currentTripsOnSelectedDate)
        }
    }

    // Trip 데이터를 TripEvent로 변환
    val tripEvents = viewModel.myTripList.mapIndexed { index, trip ->
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val startDate = LocalDate.parse(trip.startDate, formatter)
            val endDate = LocalDate.parse(trip.endDate, formatter)

            TripEvent(
                id = index.toString(),
                title = trip.location,
                startDate = startDate,
                endDate = endDate,
                color = getTripColor(index)
            )
        } catch (e: Exception) {
            TripEvent(
                id = index.toString(),
                title = trip.location,
                startDate = LocalDate.now(),
                endDate = LocalDate.now(),
                color = getTripColor(index)
            )
        }
    }

    // 선택된 날짜의 여행들 필터링
    val tripsOnSelectedDate = viewModel.myTripList.filter { trip ->
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val start = LocalDate.parse(trip.startDate, formatter)
            val end = LocalDate.parse(trip.endDate, formatter)
            !selectedDate.isBefore(start) && !selectedDate.isAfter(end)
        } catch (e: Exception) {
            false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        item {
            TopAppBar(
                title = { Text("캘린더") },
                navigationIcon = {
                    IconButton(onClick = { onNavigateToPrev() }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }

        item {
            CalendarView(
                initialDate = selectedDate,
                tripEvents = tripEvents,
                onDateSelected = { selectedDate = it }
            )
        }

        if (tripsOnSelectedDate.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "📅 ${selectedDate.format(DateTimeFormatter.ofPattern("M월 d일"))} 여행 일정",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        items(tripsOnSelectedDate) { trip ->
            TripItem(
                trip = trip,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 캘린더 등록 버튼 - 권한 요청 포함
        item {
            Spacer(modifier = Modifier.height(16.dp))

            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = {
                    // 권한 요청
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_CALENDAR,
                            Manifest.permission.WRITE_CALENDAR
                        )
                    )
                }
            ) {
                Text("📅 캘린더에 일정 등록하기")
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))

            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = onNavigateToSearch
            ) {
                Text("🔍 일정 추가하기 (검색화면 이동)")
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// 캘린더 등록 함수 분리
private fun registerToCalendar(context: android.content.Context, trips: List<com.example.tripcast.model.Trip>) {
    trips.forEach { trip ->
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val startDate = LocalDate.parse(trip.startDate, formatter)
            val endDate = LocalDate.parse(trip.endDate, formatter)

            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI

                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                    startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())

                putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                    endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())

                putExtra(CalendarContract.Events.TITLE, "${trip.location} 여행")
                putExtra(CalendarContract.Events.DESCRIPTION,
                    "목적지: ${trip.location}\n" +
                            "여행 기간: ${trip.startDate} ~ ${trip.endDate}\n" +
                            "날씨: ${trip.weather}")
                putExtra(CalendarContract.Events.ALL_DAY, true)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            // 에러 무시
        }
    }
}

private fun getTripColor(index: Int): Color {
    val colors = listOf(
        Color(0xFF2196F3), // 파란색
        Color(0xFF4CAF50), // 초록색
        Color(0xFFFF9800), // 주황색
        Color(0xFF9C27B0), // 보라색
        Color(0xFFF44336), // 빨간색
        Color(0xFF00BCD4), // 시안색
        Color(0xFFFFEB3B), // 노란색
        Color(0xFF795548), // 갈색
    )
    return colors[index % colors.size]
}