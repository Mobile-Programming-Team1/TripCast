package com.example.tripcast.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tripcast.R
import com.example.tripcast.firebase.TripSaver
import com.example.tripcast.firebase.DailyWeather
import com.example.tripcast.util.getWeatherInfo
import com.example.tripcast.viewmodel.MyTripViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckOverallScreen(viewModel: MyTripViewModel, onNavigateToPreferences: () -> Unit) {
    var tripitem = viewModel.myTripList.last()
    val weatherInfo = remember {
        getWeatherInfo(tripitem.startDate, tripitem.endDate, tripitem.location)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = { Text("Check Weather Info") },
            navigationIcon = {
                IconButton(onClick = { /* Navigate back */ }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Back"
                    )
                }
            }
        )

        Text(
            text = "Location",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = tripitem.location,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Traveling Dates",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "${tripitem.startDate} - ${tripitem.endDate}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "forecast",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        weatherInfo.forEachIndexed {index, item ->
            ForecastItem(
                day = item.date,
                condition = when (item.weather.toString()) {
                    "SUNNY" -> "mostly clear"
                    "CLOUDY" -> "it is cloudy"
                    "RAINY" -> "it is rainy"
                    "SNOWY" -> "it is snowy"
                    else -> "unknown"
                },
                icon = when (item.weather.toString()) {
                    "SUNNY" -> R.drawable.ic_sunny
                    "CLOUDY" -> R.drawable.ic_cloudy
                    "RAINY" -> R.drawable.ic_rainy
                    "SNOWY" -> R.drawable.ic_snowy
                    else -> R.drawable.gusto
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

        }

//        ForecastItem(
//            day = "Mar 5, Sun",
//            condition = "Mostly Clear",
//            icon = R.drawable.ic_sunny
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        ForecastItem(
//            day = "Mar 6, Mon",
//            condition = "Cloudy",
//            icon = R.drawable.ic_cloudy
//        )
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        ForecastItem(
//            day = "Mar 7, Tue",
//            condition = "Sunny",
//            icon = R.drawable.ic_sunny
//        )

        Spacer(modifier = Modifier.weight(1f))

        Button(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                val dailyWeatherList = weatherInfo.map {
                    DailyWeather(date = it.date, condition = it.weather.name)
                }
                TripSaver.saveTrip(
                    destination = tripitem.location,
                    startDate = LocalDate.parse(tripitem.startDate),
                    endDate = LocalDate.parse(tripitem.endDate),
                    weatherList = dailyWeatherList
                )
            }
            onNavigateToPreferences()
        },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirm")
        }
    }

    }



@Composable
fun ForecastItem(
    day: String,
    condition: String,
    icon: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = condition,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = condition,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
