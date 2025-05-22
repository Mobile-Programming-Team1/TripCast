package com.example.tripcast

//import com.example.tripcast.ui.screens.CheckWeatherScreen
//import com.example.tripcast.ui.screens.HomeScreen
//import com.example.tripcast.ui.screens.SearchDestinationScreen
//import com.example.tripcast.ui.screens.SelectDatesScreen
//import com.example.tripcast.ui.screens.WeatherCalendarScreen
//import com.example.tripcast.ui.screens.WeatherPreferencesScreen
//import com.example.tripcast.ui.theme.tripcastTheme
//import com.example.tripcast.ui.screens.ExploreScreen
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tripcast.ui.components.BottomNavBar
import com.example.tripcast.ui.screens.*
import com.example.tripcast.ui.theme.tripcastTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestCalendarPermissions()

        setContent {
            tripcastTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TripcastApp()
                }
            }
        }
    }

    // ✅ 최신 방식 권한 요청 코드
    private fun requestCalendarPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
        )

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    // ✅ registerForActivityResult 기반 처리
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultMap ->
        val allGranted = resultMap.values.all { it }
        if (allGranted) {
            Toast.makeText(this, "캘린더 권한 허용됨", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "캘린더 권한이 없으면 일정 등록이 안 됩니다", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun TripcastApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab = when (navController.currentBackStackEntryAsState().value?.destination?.route) {
                    "home" -> 0
                    "trips", "calendar", "select_dates", "check_weather", "preferences" -> 1
                    "setting", "explore" -> 2
                    else -> 0
                },
                onTabSelected = { index ->
                    when (index) {
                        0 -> navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                        1 -> navController.navigate("calendar") {
                            popUpTo("calendar")
                        }
                        2 -> navController.navigate("setting") {
                            popUpTo("setting")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(onNavigateToCalendar = { navController.navigate("calendar") })
            }
            composable("calendar") {
                CalendarScreen(onNavigateToSearch = { navController.navigate("search") })
            }
            composable("setting") {
                SettingScreen()
            }
                composable("search") {
                SearchDestinationScreen(
//                    onNavigateToSelectDates = { navController.navigate("select_dates") }
                )
            }
//            composable("select_dates") {
//                SelectDatesScreen(
//                    onNavigateToCheckWeather = { navController.navigate("check_weather") }
//                )
//            }
//            composable("check_weather") {
//                CheckWeatherScreen(
//                    onNavigateToPreferences = { navController.navigate("preferences") }
//                )
//            }
//            composable("preferences") {
//                WeatherPreferencesScreen(
//                    onGetRecommendations = { /* Handle recommendations */ }
//                )
//            }

        }
    }
}
