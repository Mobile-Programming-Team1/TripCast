package com.example.tripcast.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.tripcast.R

@Composable
fun BottomNavBar(
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {}
) {
    val items = listOf(
        NavItem("홈", R.drawable.baseline_home_24, R.drawable.outline_home_24),
        NavItem("여행", R.drawable.baseline_explore_24, R.drawable.outline_explore_24),
        NavItem("추천", R.drawable.baseline_thumb_up_alt_24, R.drawable.baseline_thumb_up_off_alt_24)
    )

    NavigationBar(
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = if (selectedTab == index) item.filledIconResId else item.outlinedIconResId),
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = selectedTab == index,
                onClick = {
                    onTabSelected(index)
                }
            )
        }
    }
}

data class NavItem(
    val title: String,
    val filledIconResId: Int,
    val outlinedIconResId: Int
)
