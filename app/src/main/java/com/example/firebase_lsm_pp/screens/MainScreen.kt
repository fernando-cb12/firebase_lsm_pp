package com.example.firebase_lsm_pp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.firebase_lsm_pp.navigation.Routes
import com.example.firebase_lsm_pp.ui.theme.AppBackground
import com.example.firebase_lsm_pp.ui.theme.Teal

@Composable
fun MainScreen(
    navController: NavController,
    content: @Composable () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = AppBackground, // Use the main background color
        bottomBar = {
            // Use a Box to center the floating navigation bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp), // Lifts the bar from the edge
                contentAlignment = Alignment.Center
            ) {
                // Surface provides the pill shape, background, and shadow
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.9f) // Make the pill wider
                        .height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    // Row to hold the navigation items
                    Row(
                        modifier = Modifier
                            .fillMaxWidth() // Ensure the Row fills the Surface
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround, // Distribute items evenly
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val bottomNavItems = listOf(
                            BottomNavItem("Home", Routes.Home.route, Icons.AutoMirrored.Filled.List),
                            BottomNavItem("Dictionary", Routes.Dictionary.route, Icons.Outlined.ThumbUp),
                            BottomNavItem("Leaderboard", Routes.Leaderboard.route, Icons.Default.Star),
                            BottomNavItem("User", Routes.User.route, Icons.Default.Person)
                        )

                        bottomNavItems.forEach { item ->
                            val selected = currentRoute == item.route
                            val contentColor = if (selected) Color.White else Color.Black.copy(alpha = 0.7f)

                            // Custom navigation item built with a Box
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clickable(onClick = {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    })
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(
                                            color = if (selected) Teal else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(24.dp),
                                        tint = contentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            content()
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)
