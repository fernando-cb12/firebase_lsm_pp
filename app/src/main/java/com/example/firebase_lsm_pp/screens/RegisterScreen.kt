package com.example.firebase_lsm_pp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.firebase_lsm_pp.auth.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
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

        Text("Crear cuenta", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(12.dp))

        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre completo") },
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Nombre de usuario") },
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") },
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                loading = true
                error = ""

                scope.launch {
                    val success = authViewModel.registerWithEmail(
                        name = name,
                        username = username,
                        email = email,
                        password = password
                    )

                    loading = false

                    if (success) onRegisterSuccess()
                    else error = "No se pudo crear tu cuenta. Revisa tus datos."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (loading) "Creando..." else "Crear cuenta")
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = Color.Red)
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onLoginClick) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}
