package com.example.tripcast

//import com.example.tripcast.ui.screens.CheckWeatherScreen
//import com.example.tripcast.ui.screens.HomeScreen
//import com.example.tripcast.ui.screens.SearchDestinationScreen
//import com.example.tripcast.ui.screens.SelectDatesScreen
//import com.example.tripcast.ui.screens.WeatherCalendarScreen
//import com.example.tripcast.ui.screens.WeatherPreferencesScreen
//import com.example.tripcast.ui.theme.tripcastTheme
//import com.example.tripcast.ui.screens.ExploreScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tripcast.ui.components.BottomNavBar
import com.example.tripcast.ui.screens.CalendarScreen
import com.example.tripcast.ui.screens.HomeScreen
import com.example.tripcast.ui.screens.SearchDestinationScreen
import com.example.tripcast.ui.screens.SettingScreen
import com.example.tripcast.ui.theme.tripcastTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            tripcastTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    tripcastApp()
                }
            }
        }
    }
}

@Composable
fun tripcastApp() {
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
                HomeScreen(
                    onNavigateToCalendar = { navController.navigate("calendar") }
                )
            }
            composable("calendar") {
                CalendarScreen(
                    onNavigateToSearch = { navController.navigate("search") }
                )
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
