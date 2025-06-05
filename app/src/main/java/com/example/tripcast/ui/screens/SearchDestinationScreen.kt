package com.example.tripcast.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.example.tripcast.util.getAvailableLocation
import com.example.tripcast.viewmodel.MyTripViewModel
import kotlinx.coroutines.delay
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDestinationScreen(
    onNavigateToSelectDates: () -> Unit, viewModel: MyTripViewModel, onNavigateToPrev:() -> Unit
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

//    val allDestinations = DestinationDataFactory.makeDestinationList()
    var allDestinations by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    val filteredDestinations = allDestinations.filter {
        it.contains(searchQuery, ignoreCase = true)
    }

    Log.d("Dropdown", "Query: $searchQuery, Matched: ${filteredDestinations.map { it }}")

    LaunchedEffect(Unit) {
        try {
            while (true) {
                val fetched = getAvailableLocation()

                allDestinations = fetched
                delay(1000000)
            }
        } catch (e: CancellationException) {
            println("작업이 취소됨")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = { Text("목적지 검색") },
            navigationIcon = {
                IconButton(onClick = { onNavigateToPrev() }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Back"
                    )
                }
            }
        )

        Text(
            text = "어디로 떠나시나요?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(modifier = Modifier.fillMaxWidth()
            ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    isDropdownExpanded = true  // 여기에 넣는 것이 안전함
                },
                modifier = Modifier
                    .fillMaxWidth(),
                placeholder = { Text("도시 이름을 입력하세요") },
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
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                properties = PopupProperties(focusable = false)
            ) {
                filteredDestinations.take(10).forEach { destination ->
                    DropdownMenuItem(
                        text = { Text(destination) },
                        onClick = {
                            searchQuery = destination
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
                    if (searchQuery.isEmpty()) {
                        Toast.makeText(context, "도시명을 입력해주세요", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    else if(!allDestinations.contains(searchQuery)) {
                        Toast.makeText(context, "존재하지 않는 도시명입니다", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
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
            Text("다음")
        }
    }
}