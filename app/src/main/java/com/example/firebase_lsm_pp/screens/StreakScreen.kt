package com.example.firebase_lsm_pp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.firebase_lsm_pp.auth.AuthRepository
import com.example.firebase_lsm_pp.auth.FirestoreUserService
import com.example.firebase_lsm_pp.navigation.Routes
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (loading) {
            CircularProgressIndicator()
        } else if (error != null) {
            Text(
                text = error ?: "Error desconocido",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Text(
                text = "Racha actual",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "$streak",
                style = MaterialTheme.typography.displayLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (streak == 1) "día" else "días",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

