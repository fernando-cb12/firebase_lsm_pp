package com.example.firebase_lsm_pp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.firebase_lsm_pp.navigation.Routes

@Composable
fun MainScreen(
    navController: NavController,
    content: @Composable () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                val bottomNavItems = listOf(
                    BottomNavItem("Home", Routes.Home.route, Icons.Default.Home),
                    BottomNavItem("Leaderboard", Routes.Leaderboard.route, Icons.Default.Star),
                    BottomNavItem("Dictionary", Routes.Dictionary.route, Icons.Default.List),
                    BottomNavItem("User", Routes.User.route, Icons.Default.Person)
                )

                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = androidx.compose.ui.Modifier.padding(paddingValues)) {
            content()
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)

