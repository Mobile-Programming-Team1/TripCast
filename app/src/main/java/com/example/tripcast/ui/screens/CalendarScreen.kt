package com.example.tripcast.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.tripcast.ui.components.CalendarView
import com.example.tripcast.util.CalendarUtil
import java.time.LocalDate
import java.util.*

@Composable
fun CalendarScreen(modifier: Modifier = Modifier, onNavigateToSearch: () -> Unit) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            CalendarView(
                initialDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )
        }

        // ✅ 캘린더 등록 버튼
        item {
            FilledTonalButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val startMillis = Calendar.getInstance().apply {
                        set(
                            selectedDate.year,
                            selectedDate.monthValue - 1,
                            selectedDate.dayOfMonth,
                            10, 0
                        )
                    }.timeInMillis

                    val endMillis = startMillis + 2 * 60 * 60 * 1000 // +2시간

                    CalendarUtil.insertEvent(
                        context = context,
                        title = "TripCast 일정",
                        description = "이 날은 여행이다!",
                        startMillis = startMillis,
                        endMillis = endMillis
                    )
                }
            ) {
                Text("📅 캘린더에 일정 등록하기")
            }
        }

        // 🔁 기존 검색화면 이동 버튼
        item {
            FilledTonalButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToSearch
            ) {
                Text("🔍 일정 추가하기 (검색화면 이동)")
            }
        }
    }
}
