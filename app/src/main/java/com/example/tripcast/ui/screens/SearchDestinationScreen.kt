package com.example.tripcast.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.example.tripcast.model.DestinationDataFactory
import com.example.tripcast.viewmodel.MyTripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDestinationScreen(
    onNavigateToSelectDates: () -> Unit, viewModel: MyTripViewModel
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val allDestinations = DestinationDataFactory.makeDestinationList()
    val filteredDestinations = allDestinations.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Log.d("Dropdown", "Query: $searchQuery, Matched: ${filteredDestinations.map { it.name }}")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Where to?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    isDropdownExpanded = true  // 여기에 넣는 것이 안전함
                },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = { Text("Search for a city or airport") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                )
            )

            DropdownMenu(
                expanded = isDropdownExpanded && filteredDestinations.isNotEmpty(),
                onDismissRequest = {
                    isDropdownExpanded = false
                },
                modifier = Modifier
                    .fillMaxWidth(),
                properties = PopupProperties(focusable = false)
            ) {
                filteredDestinations.forEach { destination ->
                    DropdownMenuItem(
                        text = { Text(destination.name) },
                        onClick = {
                            searchQuery = destination.name
                            isDropdownExpanded = false
                            focusManager.clearFocus()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                try {
                    Log.d("Tripcast", "Before addEmptyTrip")
                    viewModel.addEmptyTrip(searchQuery)
                    Log.d("Tripcast", "After addEmptyTrip")
                    onNavigateToSelectDates()
                } catch (e: Exception) {
                    Log.e("Tripcast", "Error during trip add/searchDestinationScreen: ${e.message}")
                }
                      },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Next")
        }
    }
}