package com.example.tripcast.ui.screens
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tripcast.MyFirebaseMessagingService
import com.example.tripcast.R
import com.example.tripcast.model.Trip
import com.example.tripcast.viewmodel.MyTripViewModel

@Composable
fun HomeScreen(
    onNavigateToCalendar: () -> Unit, viewModel: MyTripViewModel
) {
    LaunchedEffect(MyFirebaseMessagingService.token) {
        MyFirebaseMessagingService.token?.let { token ->
            viewModel.loadTripsFromFirebase(token)
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Trip Cast",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.worldtour),
                        contentDescription = "City Aerial View",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "나의 여행 계획",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "날씨에 기반한 여행계획을 세우세요!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        // 내 일정들
        items(viewModel.myTripList) { trip ->
            TripItem(trip = trip, onDelete = { viewModel.removeTrip(trip) })
            Spacer(modifier = Modifier.height(8.dp))
        }

    }
}

@Composable
fun TripItem(trip: Trip, onDelete: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = trip.weather,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            Text(
                text = "${trip.location}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "${trip.startDate} ~ ${trip.endDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        if (onDelete != null) {
            Button(onClick = onDelete) {
                Text("삭제")
            }
        }
    }
}

//@Composable
//fun RecommendedTripItem(trip: Trip) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Box(
//            modifier = Modifier
//                .size(8.dp)
//                .background(MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(4.dp))
//        )
//
//        Spacer(modifier = Modifier.width(8.dp))
//
//        Column {
//            Text(
//                text = trip.weather,
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//            )
//
//            Text(
//                text = trip.location,
//                style = MaterialTheme.typography.bodyLarge,
//                fontWeight = FontWeight.SemiBold
//            )
//
//            Text(
//                text = trip.location,
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
//            )
//        }
//    }
//}