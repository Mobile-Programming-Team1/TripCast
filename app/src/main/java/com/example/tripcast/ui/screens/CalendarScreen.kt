package com.example.tripcast.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tripcast.ui.components.CalendarView
import com.example.tripcast.viewmodel.MyTripViewModel

@Composable
fun CalendarScreen(modifier: Modifier = Modifier, onNavigateToSearch: () -> Unit, viewModel: MyTripViewModel) {
    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        item {
            CalendarView()
        }
        item {
            FilledTonalButton(modifier= Modifier.fillMaxWidth(), onClick = onNavigateToSearch) {
                Text("일정 추가하기")
            }
        }
    }
}