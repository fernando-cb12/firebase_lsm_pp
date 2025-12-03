package com.example.firebase_lsm_pp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.firebase_lsm_pp.auth.AuthRepository
import com.example.firebase_lsm_pp.auth.FirestoreUserService
import com.example.firebase_lsm_pp.navigation.Routes
import com.example.firebase_lsm_pp.ui.theme.AppAccent
import com.example.firebase_lsm_pp.ui.theme.AppBackground
import com.example.firebase_lsm_pp.ui.theme.AppTextPrimary
import com.example.firebase_lsm_pp.ui.theme.AppYellow
import kotlinx.coroutines.delay

@Composable
fun StreakScreen(navController: NavController) {
    var streak by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val authRepo = remember { AuthRepository() }
    val userService = remember { FirestoreUserService() }

    LaunchedEffect(Unit) {
        val currentUser = authRepo.getCurrentUser()
        if (currentUser == null) {
            error = "Usuario no autenticado"
            loading = false
            return@LaunchedEffect
        }

        try {
            val user = userService.getUser(currentUser.uid)
            streak = user?.streak ?: 0
            loading = false
        } catch (e: Exception) {
            error = "Error al cargar el streak: ${e.message}"
            loading = false
        }

        // Navigate to HomeScreen after 3 seconds
        delay(3000)
        navController.navigate(Routes.Home.route) {
            popUpTo(Routes.Streak.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = AppAccent
                )
            } else if (error != null) {
                Text(
                    text = error ?: "Error desconocido",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    text = "Racha actual",
                    style = MaterialTheme.typography.headlineLarge,
                    color = AppAccent,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Streak number with fire icon
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Star icon behind the number
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Streak",
                        tint = AppYellow.copy(alpha = 0.3f),
                        modifier = Modifier.size(200.dp)
                        
                    )

                    // Number on top
                    Text(
                        text = "$streak",
                        style = MaterialTheme.typography.displayLarge,
                        color = AppAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 80.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (streak == 1) "día" else "días",
                    style = MaterialTheme.typography.titleLarge,
                    color = AppTextPrimary.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
