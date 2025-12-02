package com.example.firebase_lsm_pp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.firebase_lsm_pp.auth.firebase.FirebaseAuthService
import com.example.firebase_lsm_pp.navigation.Routes

@Composable
fun HomeScreen(navController: NavController) {

    val auth = remember { FirebaseAuthService() }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("¡Registro / Login funcionando!")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            auth.logout()
            navController.navigate(Routes.Login.route) {
                popUpTo(Routes.Home.route) { inclusive = true }
            }
        }) {
            Text("Cerrar sesión")
        }
    }
}
