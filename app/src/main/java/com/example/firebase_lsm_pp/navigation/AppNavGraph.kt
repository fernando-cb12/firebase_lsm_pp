package com.example.firebase_lsm_pp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.firebase_lsm_pp.auth.AuthViewModel
import com.example.firebase_lsm_pp.components.shared.GoogleUsernameScreen
import com.example.firebase_lsm_pp.screens.HomeScreen
import com.example.firebase_lsm_pp.screens.LoginScreen
import com.example.firebase_lsm_pp.screens.RegisterScreen
import com.example.firebase_lsm_pp.screens.StreakScreen

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object Register : Routes("register")
    object Home : Routes("home")
    object GoogleUsername : Routes("google_username")
    object Streak : Routes("streak")
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val authViewModel = AuthViewModel()

    NavHost(navController = navController, startDestination = Routes.Login.route) {

        /** LOGIN */
        composable(Routes.Login.route) {
            LoginScreen(
                navController = navController,
                viewModel = authViewModel
            )
        }

        /** REGISTER */
        composable(Routes.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Routes.Streak.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate(Routes.Login.route)
                }
            )
        }

        /** HOME */
        composable(Routes.Home.route) {
            HomeScreen(navController)
        }

        /** GOOGLE USERNAME */
        composable(Routes.GoogleUsername.route) {
            GoogleUsernameScreen(
                viewModel = authViewModel,
                onFinish = {
                    navController.navigate(Routes.Streak.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        /** STREAK */
        composable(Routes.Streak.route) {
            StreakScreen(navController = navController)
        }
    }
}
