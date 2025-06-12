package com.example.tripcast.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tripcast.MyFirebaseMessagingService
import com.example.tripcast.R
import com.example.tripcast.firebase.DailyWeather
import com.example.tripcast.firebase.TripSaver
import com.example.tripcast.model.DayInfo
import com.example.tripcast.model.WeatherInfo
import com.example.tripcast.util.getAirInfo
import com.example.tripcast.util.getWeatherInfo
import com.example.tripcast.viewmodel.MyTripViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckOverallScreen(
    viewModel: MyTripViewModel,
    onNavigateToPreferences: () -> Unit,
    onNavigateToPrev: () -> Unit
) {
    LaunchedEffect(Unit) {
        MyFirebaseMessagingService.fetchAndLogToken()
    }
    var tripitem = viewModel.myTripList.last()
//    val weatherInfo = remember {
//        getWeatherInfo(tripitem.startDate, tripitem.endDate, tripitem.location)
//    }
    // 날씨 정보를 저장할 상태 변수
    var weatherInfoList by remember { mutableStateOf<List<WeatherInfo>>(emptyList()) }
    // 미세먼지 정보를 저장할 상태 변수
    var airInfoList by remember { mutableStateOf<List<Int>>(emptyList()) }
    // 로딩 상태 표시를 위한 상태 변수
    var isLoadingWeather by remember { mutableStateOf(true) }
    // 스코롤 상태 저장을 위한 상태 변수
    val scrollState = rememberScrollState()
    // 날씨 정보와 미세먼지 정보를 합친 상태 리스트
    var dayInfoList by remember { mutableStateOf<List<DayInfo>>(emptyList()) }

    LaunchedEffect(key1 = true) {
        isLoadingWeather = true // 로딩 시작
        try {
            weatherInfoList =
                getWeatherInfo(tripitem.startDate, tripitem.endDate, tripitem.location)
            airInfoList = getAirInfo(tripitem.startDate, tripitem.endDate, tripitem.location)
            dayInfoList = weatherInfoList.zip(airInfoList) { weatherItem, airValue ->
                DayInfo(
                    date = weatherItem.date,
                    weather = weatherItem.weather,
                    air = airValue
                )

            }
        } catch (e: Exception) {
            Log.e("CheckOverallScreen", "Failed to load weather info", e)
            weatherInfoList = emptyList() // 오류 발생 시 빈 리스트로 설정
        } finally {
            isLoadingWeather = false // 로딩 완료
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        TopAppBar(
            title = { Text("전체 일정 확인") },
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
            text = "위치",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = tripitem.location,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "일정",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "${tripitem.startDate} - ${tripitem.endDate}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "일기예보",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isLoadingWeather) {
            Row(
                modifier = Modifier.fillMaxSize().weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator()
            }
        } else if (dayInfoList.isEmpty()) {
            Text("날씨 정보를 불러 올수 없습니다")
        } else {
            dayInfoList.forEachIndexed { index, item ->
                ForecastItem(
                    day = item.date,
                    weatherCondition = when (item.weather.toString()) {
                        "CLEAR" -> "화창합니다!"
                        "CLOUDS" -> "구름이 많습니다!"
                        "RAIN" -> "비가 내립니다!"
                        "SNOW" -> "눈이 옵니다!"
                        "THUNDERSTORM" -> "천둥번개가 칩니다!"
                        "DRIZZLE" -> "약한 비가 내립니다!"
                        "MIST", "FOG", "HAZE" -> "안개가 자욱합니다!"
                        "SMOKE" -> "연기가 많습니다!"
                        "DUST", "SAND" -> "모래먼지가 많습니다!"
                        "ASH" -> "재가 날립니다!"
                        "SQUALL" -> "돌풍이 붑니다!"
                        "TORNADO" -> "토네이도가 칩니다!"
                        else -> "알 수 없는 날씨"
                    },
                    weatherIcon = when (item.weather.toString()) {
                        "CLEAR" -> R.drawable.ic_sunny
                        "CLOUDS" -> R.drawable.ic_cloudy
                        "RAIN" -> R.drawable.ic_rainy
                        "SNOW" -> R.drawable.ic_snowy
                        "THUNDERSTORM" -> R.drawable.baseline_thunderstorm_24
                        "DRIZZLE" -> R.drawable.baseline_water_drop_24
                        "MIST" -> R.drawable.baseline_question_mark_24
                        "SMOKE" -> R.drawable.baseline_question_mark_24
                        "HAZE" -> R.drawable.baseline_question_mark_24
                        "DUST" -> R.drawable.baseline_question_mark_24
                        "FOG" -> R.drawable.baseline_question_mark_24
                        "SAND" -> R.drawable.baseline_question_mark_24
                        "ASH" -> R.drawable.baseline_question_mark_24
                        "SQUALL" -> R.drawable.baseline_question_mark_24
                        "TORNADO" -> R.drawable.baseline_question_mark_24
                        else -> R.drawable.gusto
                    },
                    airCondition = when (item.air) {
                        1 -> "공기질 최상"
                        2 -> "공기질 좋음"
                        3 -> "공기질 보통"
                        4 -> "공기질 별로"
                        5 -> "공기질 최악"
                        else -> "정의되지 않음"
                    },
                    airIcon = when (item.air) {
                        1 -> R.drawable.baseline_looks_one_24
                        2 -> R.drawable.baseline_looks_two_24
                        3 -> R.drawable.baseline_looks_3_24
                        4 -> R.drawable.baseline_looks_4_24
                        5 -> R.drawable.baseline_looks_5_24
                        else -> R.drawable.baseline_question_mark_24
                    }

                )
                Spacer(modifier = Modifier.height(8.dp))

            }
        }




        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    val dailyWeatherList = weatherInfoList.zip(airInfoList) { weather, air ->
                        DailyWeather(date = weather.date, condition = weather.weather.name, fineDust = air)
                    }
                    TripSaver.saveTrip(
                        destination = tripitem.location,
                        startDate = LocalDate.parse(tripitem.startDate),
                        endDate = LocalDate.parse(tripitem.endDate),
                        weatherList = dailyWeatherList,
                        userToken = MyFirebaseMessagingService.token ?: "UNKNOWN"                    )

                }
                onNavigateToPreferences()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("확정")
        }
    }

}


@Composable
fun ForecastItem(
    day: String,
    weatherCondition: String,
    weatherIcon: Int,
    airCondition: String,
    airIcon: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Row {
                Icon(
                    painter = painterResource(id = weatherIcon),
                    contentDescription = weatherCondition,
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
                        text = weatherCondition,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Row {
                Icon(
                    painter = painterResource(id = airIcon),
                    contentDescription = airCondition,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = airCondition,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

        }
    }
}
