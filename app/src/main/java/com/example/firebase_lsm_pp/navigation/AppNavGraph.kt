package com.example.firebase_lsm_pp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import com.example.firebase_lsm_pp.auth.AuthViewModel
import com.example.firebase_lsm_pp.auth.UserData
import com.example.firebase_lsm_pp.components.shared.GoogleUsernameScreen
import com.example.firebase_lsm_pp.screens.DictionaryScreen
import com.example.firebase_lsm_pp.screens.HomeScreen
import com.example.firebase_lsm_pp.screens.LeaderboardScreen
import com.example.firebase_lsm_pp.screens.LessonDetailScreen
import com.example.firebase_lsm_pp.screens.LoginScreen
import com.example.firebase_lsm_pp.screens.MainScreen
import com.example.firebase_lsm_pp.screens.RegisterScreen
import com.example.firebase_lsm_pp.screens.StreakScreen
import com.example.firebase_lsm_pp.screens.ProfileScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object Register : Routes("register")
    object Home : Routes("home")
    object GoogleUsername : Routes("google_username")
    object Streak : Routes("streak")
    object Leaderboard : Routes("leaderboard")
    object User : Routes("user")
    object Dictionary : Routes("dictionary")
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

        /** HOME */
        composable(Routes.Home.route) {
            MainScreen(navController = navController) {
                HomeScreen(navController)
            }
        }

        /** LEADERBOARD */
        composable(Routes.Leaderboard.route) {
            MainScreen(navController = navController) {
                LeaderboardScreen()
            }
        }

        /** DICTIONARY */
        composable(Routes.Dictionary.route) {
            MainScreen(navController = navController) {
                DictionaryScreen()
            }
        }

        /** USER */
        composable(Routes.User.route) {
            var userData by remember { mutableStateOf<UserData?>(null) }
            LaunchedEffect(key1 = Unit) {
                userData = authViewModel.getSignedInUser()
            }
            MainScreen(navController = navController) {
                ProfileScreen(
                    userData = userData,
                    onSignOut = {
                        authViewModel.signOut()
                        navController.navigate(Routes.Login.route) {
                            popUpTo(Routes.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        /** LESSON DETAIL */
        composable(
            route = "lesson/{lessonTitle}",
            arguments = listOf(
                navArgument("lessonTitle") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val lessonTitle = backStackEntry.arguments?.getString("lessonTitle") ?: ""
            LessonDetailScreen(
                navController = navController,
                lessonTitle = lessonTitle
            )
        }
    }
}