package com.example.tripcast.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tripcast.R
import com.example.tripcast.ui.components.CalendarView

data class WeatherPreference(
    val name: String,
    val isSelected: Boolean = false
)

data class Destination(
    val name: String,
    val weather: String,
    val temperature: String,
    val imageResId: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceScreen(
    onGetRecommendations: () -> Unit
) {
    var weatherPreferences by remember {
        mutableStateOf(
            listOf(
                WeatherPreference("Sunny", true),
                WeatherPreference("Warm", false),
                WeatherPreference("Dry", false),
                WeatherPreference("Mild", false),
                WeatherPreference("Cool", false),
                WeatherPreference("Cold", false)
            )
        )
    }

    val destinations = listOf(
        Destination("Carmel-by-the-sea", "Mostly Sunny", "68°", R.drawable.gusto),
        Destination("Pacific Grove", "Mostly Sunny", "72°", R.drawable.palmer),
        Destination("Monterey", "Mostly Sunny", "71°", R.drawable.gusto)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("Weather Preferences") },
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
            text = "What's your ideal weather?",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weatherPreferences.take(3).forEach { preference ->
                WeatherPreferenceItem(
                    preference = preference,
                    onClick = {
                        weatherPreferences = weatherPreferences.map {
                            if (it.name == preference.name) it.copy(isSelected = !it.isSelected)
                            else it
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weatherPreferences.takeLast(3).forEach { preference ->
                WeatherPreferenceItem(
                    preference = preference,
                    onClick = {
                        weatherPreferences = weatherPreferences.map {
                            if (it.name == preference.name) it.copy(isSelected = !it.isSelected)
                            else it
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Jan 2023",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = { /* Previous month */ }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous Month"
                )
            }

            IconButton(onClick = { /* Next month */ }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next Month"
                )
            }
        }

        CalendarView()

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onGetRecommendations,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Recommendations")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Recommended Destinations",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        destinations.forEach { destination ->
            DestinationRecommendationItem(destination = destination)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun WeatherPreferenceItem(
    preference: WeatherPreference,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    if (preference.isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
                )
                .border(
                    width = 1.dp,
                    color = if (preference.isSelected) MaterialTheme.colorScheme.primary
                    else Color.Gray.copy(alpha = 0.5f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = preference.name.first().toString(),
                color = if (preference.isSelected) Color.White
                else MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = preference.name,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun DestinationRecommendationItem(
    destination: Destination
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Image(
                painter = painterResource(id = destination.imageResId),
                contentDescription = destination.name,
                modifier = Modifier
                    .width(120.dp)
                    .height(80.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = destination.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${destination.weather}, ${destination.temperature}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
