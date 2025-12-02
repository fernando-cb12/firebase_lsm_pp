package com.example.firebase_lsm_pp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.firebase_lsm_pp.auth.AuthViewModel
import com.example.firebase_lsm_pp.auth.firebase.FirebaseAuthService
import com.example.firebase_lsm_pp.components.shared.GoogleSignInButton
import com.example.firebase_lsm_pp.navigation.Routes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") }
        )

        Spacer(Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(16.dp))

        // LOGIN NORMAL
        Button(
            onClick = {
                loading = true
                CoroutineScope(Dispatchers.Main).launch {
                    val ok = viewModel.loginWithEmail(email, password)
                    loading = false
                    if (ok) {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Login.route) { inclusive = true }
                        }
                    } else {
                        error = "Credenciales incorrectas"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Entrando..." else "Entrar")
        }

        Spacer(Modifier.height(14.dp))

        // GOOGLE LOGIN BUTTON
        GoogleSignInButton(
            onResult = { user ->
                if (user == null) {
                    error = "Error al iniciar con Google"
                    return@GoogleSignInButton
                }

                CoroutineScope(Dispatchers.Main).launch {
                    val isNew = viewModel.handleGoogleLogin(user)

                    if (isNew) {
                        navController.navigate(Routes.GoogleUsername.route)
                    } else {
                        navController.navigate(Routes.Home.route) {
                            popUpTo(Routes.Login.route) { inclusive = true }
                        }
                    }
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate(Routes.Register.route) }
        ) {
            Text("¿No tienes cuenta? Regístrate")
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = Color.Red)
        }
    }
}

