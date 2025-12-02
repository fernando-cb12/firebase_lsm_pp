package com.example.firebase_lsm_pp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.firebase_lsm_pp.screens.HomeScreen
import com.example.firebase_lsm_pp.screens.LoginScreen
import com.example.firebase_lsm_pp.screens.RegisterScreen


sealed class Routes(val route: String) {
    object Login : Routes("login")
    object Register : Routes("register")
    object Home : Routes("home")
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.Login.route) {

        composable(Routes.Login.route) {
            LoginScreen(navController)
        }
        composable(Routes.Register.route) {
            RegisterScreen(navController)
        }
        composable(Routes.Home.route) {
            HomeScreen(navController)
        }
    }
}
