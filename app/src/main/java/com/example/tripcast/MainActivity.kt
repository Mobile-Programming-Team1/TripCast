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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tripcast.ui.components.BottomNavBar
import com.example.tripcast.ui.screens.CalendarScreen
import com.example.tripcast.ui.screens.CheckOverallScreen
import com.example.tripcast.ui.screens.HomeScreen
import com.example.tripcast.ui.screens.PreferenceScreen
import com.example.tripcast.ui.screens.SearchDestinationScreen
import com.example.tripcast.ui.screens.SelectDatesScreen
import com.example.tripcast.ui.screens.SettingScreen
import com.example.tripcast.ui.theme.tripcastTheme
import com.example.tripcast.viewmodel.MyTripViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission for Android 13+ (TIRAMISU)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        requestCalendarPermissions()

        setContent {
            tripcastTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyFirebaseMessagingService.fetchAndLogToken()
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
    var myTripViewModel : MyTripViewModel = viewModel()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedTab = when (navController.currentBackStackEntryAsState().value?.destination?.route) {
                    "home" -> 0
                    "search", "calendar", "select_dates", "check_overall", "preferences" -> 1
                    "preference" -> 2
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
                        2 -> navController.navigate("preference") {
                            popUpTo("preference")
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
                    viewModel = myTripViewModel,
                    onNavigateToCalendar = { navController.navigate("calendar") }
                )
            }
            composable("calendar") {
                CalendarScreen(
                    viewModel = myTripViewModel,
                    onNavigateToSearch = { navController.navigate("search") },
                    onNavigateToPrev = {navController.popBackStack()}
                )
            }
            composable("setting") {
                SettingScreen()
            }

            composable("search") {

                SearchDestinationScreen(
                    viewModel = myTripViewModel,
                    onNavigateToSelectDates = {navController.navigate("select_dates") },
                    onNavigateToPrev = {navController.popBackStack()}
                )
            }
            composable("select_dates") {
                SelectDatesScreen(
                    viewModel = myTripViewModel,
                    onNavigateToCheckOverall = { navController.navigate("check_overall") },
                    onNavigateToPrev = {navController.popBackStack()}
                )
            }
            composable("check_overall") {
                CheckOverallScreen(
                    viewModel = myTripViewModel,
                    onNavigateToPreferences = { navController.navigate("home") },
                    onNavigateToPrev = {navController.popBackStack()}
                )
            }
            composable("preference") {
                PreferenceScreen {  }
            }

        }
    }
}
