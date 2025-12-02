package com.example.firebase_lsm_pp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.firebase_lsm_pp.auth.firebase.FirebaseAuthService
import com.example.firebase_lsm_pp.components.shared.GoogleSignInButton
import com.example.firebase_lsm_pp.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(navController: NavController) {

    val auth = remember { FirebaseAuthService() }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        Spacer(modifier = Modifier.height(12.dp))

        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            scope.launch {
                val result = auth.register(email, password)
                if (result.isSuccess) {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Register.route) { inclusive = true }
                    }
                } else {
                    errorMessage = result.exceptionOrNull()?.message
                }
            }
        }) {
            Text("Registrarse")
        }
        Spacer(modifier = Modifier.height(20.dp))

        GoogleSignInButton(text = "Registrarse con Google") { user ->
            if (user != null) {
                navController.navigate(Routes.Home.route) {
                    popUpTo(Routes.Register.route) { inclusive = true }
                }
            } else {
                errorMessage = "Error al registrarse con Google"
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate(Routes.Login.route) }) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}
