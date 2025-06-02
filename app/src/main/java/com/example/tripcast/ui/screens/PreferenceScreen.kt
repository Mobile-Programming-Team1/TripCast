package com.example.tripcast.ui.screens

import android.util.Log
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tripcast.ui.components.CalendarView
import com.example.tripcast.util.getRecommendInfo
import kotlinx.coroutines.launch
import java.time.LocalDate

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
fun PreferenceScreen() {
    var isLoading by remember { mutableStateOf(false) }

    var weatherPreferences by remember {
        mutableStateOf(
            listOf(
                WeatherPreference("Thunderstorm", false),
                WeatherPreference("Drizzle", false),
                WeatherPreference("Rain", false),
                WeatherPreference("Snow", false),
                WeatherPreference("Mist", false),
                WeatherPreference("Smoke", false),
                WeatherPreference("Haze", false),
                WeatherPreference("Dust", false),
                WeatherPreference("Fog", false),
                WeatherPreference("Sand", false),
                WeatherPreference("Ash", false),
                WeatherPreference("Squall", false),
                WeatherPreference("Tornado", false),
                WeatherPreference("Clear", false),
                WeatherPreference("Clouds", false)
            )
        )
    }

    val coroutineScope = rememberCoroutineScope()

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedWeather by remember { mutableStateOf("") }
    
    // 추천 여행지 리스트
    var recommendations by remember { mutableStateOf<List<String>>(emptyList()) }
    // 스크롤 상태 변수
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
//        TopAppBar(
//            title = { Text("선호 날씨에 따른 여행지 추천") },
////            navigationIcon = {
////                IconButton(onClick = { /* Navigate back */ }) {
////                    Icon(
////                        imageVector = Icons.Default.KeyboardArrowLeft,
////                        contentDescription = "Back"
////                    )
////                }
////            }
//        )
        Text(
            text = "선호 날씨에 따른 여행지 추천",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "당신이 좋아하는 날씨를 터치하세요!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        weatherPreferences
            .chunked(5) // 리스트를 5개씩 끊어서 List<List<WeatherPreference>> 형태로 만듦
            .forEach { rowList ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowList.forEach { preference ->
                        WeatherPreferenceItem(
                            preference = preference,
                            onClick = {
                                // 클릭하면 해당 항목의 isSelected 토글
                                weatherPreferences = weatherPreferences.map {
                                    if (it.name == preference.name) {
                                        it.copy(isSelected = true)
                                    } else {
                                        it.copy(isSelected = false)
                                    }
                                }
                                selectedWeather = preference.name
                                Log.d("selectedWeather", selectedWeather)
                            }
                        )
                    }
                    // 만약 마지막 행에 5개 미만 남았을 경우 빈 칸 메우기
                    if (rowList.size < 5) {
                        // 예: rowList.size == 3이면 2개의 Spacer를 추가해서 간격 맞추기
                        repeat(5 - rowList.size) {
                            Spacer(modifier = Modifier.width(60.dp)) // 아이콘 크기와 동일한 폭
                        }
                    }
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

        CalendarView(onDateSelected = {date -> selectedDate = date
            Log.d("selectedDate", selectedDate.toString())
        })

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    launch {
                        kotlinx.coroutines.delay(300)
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                    val resultList: List<String> = getRecommendInfo(
                        date = selectedDate.toString(),
                        weather = selectedWeather
                    )
                    isLoading = false
                    recommendations = resultList
                    Log.d("RecommendationResult", recommendations.toString())
                    launch {
                        kotlinx.coroutines.delay(300)
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("추천받기")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "추천 여행지 목록",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if(isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            recommendations.forEach { destination ->
                DestinationRecommendationItem(destination = destination)
                Spacer(modifier = Modifier.height(16.dp))
            }
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
            text = if (preference.name != "Thunderstorm") preference.name else "Thunder\n  storm" ,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun DestinationRecommendationItem(
    destination: String
) {
    val context = LocalContext.current
    val resId = remember(destination) {
        context.resources.getIdentifier(destination.lowercase(), "drawable", context.packageName)
    }

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
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = destination,
                    modifier = Modifier
                        .width(120.dp)
                        .height(80.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("이미지 없음: $destination")
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = destination,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

//                Text(
//                    text = "${weatherS}, ${destination.temperature}",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//                )
            }
        }
    }
}
