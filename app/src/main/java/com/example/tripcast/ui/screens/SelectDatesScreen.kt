package com.example.tripcast.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tripcast.ui.components.CalendarView
import com.example.tripcast.viewmodel.MyTripViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectDatesScreen(
    viewModel: MyTripViewModel,
    onNavigateToCheckOverall: () -> Unit,
    onNavigateToPrev: () -> Unit
) {
    val startDate = LocalDate.of(2025, 2, 1)
    val endDate = LocalDate.of(2025, 2, 3)
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    var selectedStartDate by remember { mutableStateOf(startDate) }
    var selectedEndDate by remember { mutableStateOf(endDate) }
    var enableNextButton by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

//    println(viewModel.myTripList.last())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        TopAppBar(
            title = { Text("Select Travel Dates") },
            navigationIcon = {
                IconButton(onClick = { onNavigateToPrev() }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Back"
                    )
                }
            }
        )

        CalendarView(onDateSelected = { date -> selectedStartDate = date })
        CalendarView(onDateSelected = { date -> selectedEndDate = date })

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Traveling Dates",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = if (selectedStartDate > selectedEndDate) {
                enableNextButton = false
                "유효하지 않은 날짜 범위입니다"
            } else {
                enableNextButton = true
                "${selectedStartDate.format(formatter)} - ${selectedEndDate.format(formatter)}"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (selectedStartDate > selectedEndDate) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                try {
                    viewModel.updateLastTrip(startDate=selectedStartDate.toString(), endDate=selectedEndDate.toString())
                    onNavigateToCheckOverall()
                } catch(e: Exception) {
                    Log.e("Tripcast", "Error during trip updateLastTrip/selectDatesScreen: ${e.message}")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = enableNextButton

        ) {
            Text("Next")
        }
    }
}