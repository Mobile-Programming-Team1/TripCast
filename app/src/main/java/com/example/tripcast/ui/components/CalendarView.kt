package com.example.tripcast.ui.components

import androidx.compose.foundation.background
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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalendarView(
    initialDate: LocalDate = LocalDate.now(),
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
            onDateSelected = { date ->
                selectedDate = date
                onDateSelected(date)
            }
        )
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

@Composable
private fun CalendarDays(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
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

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else Color.Transparent
                                    )
                                    .clickable { onDateSelected(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayCounter.toString(),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground
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

@Preview
@Composable
private fun CalendarViewPrev() {
    CalendarView()
}