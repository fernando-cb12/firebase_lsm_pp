package com.example.firebase_lsm_pp.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.firebase_lsm_pp.auth.AppUser
import com.example.firebase_lsm_pp.auth.AuthRepository
import com.example.firebase_lsm_pp.auth.FirestoreUserService
import com.example.firebase_lsm_pp.navigation.Routes
import com.example.firebase_lsm_pp.ui.theme.AppAccent
import com.example.firebase_lsm_pp.ui.theme.AppBackground
import com.example.firebase_lsm_pp.ui.theme.AppButtonColor
import com.example.firebase_lsm_pp.ui.theme.AppTextPrimary
import com.example.firebase_lsm_pp.ui.theme.AppYellow
import kotlinx.coroutines.launch

@Composable
fun UserScreen(navController: NavController) {
    var user by remember { mutableStateOf<AppUser?>(null) }
    var firebaseUser by remember { mutableStateOf<com.google.firebase.auth.FirebaseUser?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isEditingUsername by remember { mutableStateOf(false) }
    var editedUsername by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val userService = remember { FirestoreUserService() }
    val authRepo = remember { AuthRepository() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val currentUser = authRepo.getCurrentUser()
        if (currentUser == null) {
            error = "Usuario no autenticado"
            loading = false
            return@LaunchedEffect
        }

        firebaseUser = currentUser

        try {
            val appUser = userService.getUser(currentUser.uid)
            user = appUser
            editedUsername = appUser?.username ?: ""
            loading = false
        } catch (e: Exception) {
            Log.e("UserScreen", "Error loading user data", e)
            error = "Error al cargar los datos del usuario: ${e.message}"
            loading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with Profile Picture
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Picture
                if (firebaseUser?.photoUrl != null) {
                    AsyncImage(
                        model = firebaseUser?.photoUrl.toString(),
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder circle
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(AppButtonColor.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.username?.take(1)?.uppercase() ?: user?.name?.take(1)?.uppercase() ?: "?",
                            style = MaterialTheme.typography.headlineLarge,
                            color = AppAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Perfil de Usuario",
                    style = MaterialTheme.typography.headlineLarge,
                    color = AppAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            when {
                loading -> {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppAccent)
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                user == null -> {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontró información del usuario",
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppTextPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
                else -> {
                    val currentUser = user // Local variable to avoid smart cast issues
                    if (currentUser != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // User Info Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = androidx.compose.ui.graphics.Color(0xFF3A4550)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Username Section
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Nombre de usuario",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AppTextPrimary.copy(alpha = 0.7f),
                                                fontSize = 12.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            if (isEditingUsername) {
                                                OutlinedTextField(
                                                    value = editedUsername,
                                                    onValueChange = { editedUsername = it },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = AppTextPrimary,
                                                        unfocusedTextColor = AppTextPrimary,
                                                        focusedBorderColor = AppAccent,
                                                        unfocusedBorderColor = AppTextPrimary.copy(alpha = 0.5f)
                                                    ),
                                                    singleLine = true,
                                                    enabled = !isSaving
                                                )
                                            } else {
                                                Text(
                                                    text = currentUser.username.ifEmpty { "Sin nombre de usuario" },
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = AppTextPrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        
                                        if (!isEditingUsername) {
                                            IconButton(
                                                onClick = {
                                                    isEditingUsername = true
                                                    editedUsername = currentUser.username
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Editar nombre de usuario",
                                                    tint = AppAccent
                                                )
                                            }
                                        }
                                    }

                                    // Save/Cancel buttons when editing
                                    if (isEditingUsername) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = {
                                                    isEditingUsername = false
                                                    editedUsername = currentUser.username
                                                },
                                                modifier = Modifier.weight(1f),
                                                enabled = !isSaving,
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = AppTextPrimary
                                                )
                                            ) {
                                                Text("Cancelar")
                                            }
                                            
                                            Button(
                                                onClick = {
                                                    scope.launch {
                                                        isSaving = true
                                                        try {
                                                            val firebaseUser = authRepo.getCurrentUser()
                                                            if (firebaseUser != null && editedUsername.isNotBlank()) {
                                                                userService.updateUser(
                                                                    firebaseUser.uid,
                                                                    mapOf("username" to editedUsername.trim())
                                                                )
                                                                // Refresh user data
                                                                val updatedUser = userService.getUser(firebaseUser.uid)
                                                                user = updatedUser
                                                                isEditingUsername = false
                                                            }
                                                        } catch (e: Exception) {
                                                            Log.e("UserScreen", "Error updating username", e)
                                                            error = "Error al actualizar el nombre de usuario: ${e.message}"
                                                        } finally {
                                                            isSaving = false
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                                enabled = !isSaving && editedUsername.isNotBlank(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = AppButtonColor
                                                )
                                            ) {
                                                if (isSaving) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        color = AppAccent,
                                                        strokeWidth = 2.dp
                                                    )
                                                } else {
                                                    Text("Guardar")
                                                }
                                            }
                                        }
                                    }

                                    Divider(color = AppTextPrimary.copy(alpha = 0.3f))

                                    // Name (if different from username)
                                    if (currentUser.name.isNotEmpty() && currentUser.name != currentUser.username) {
                                        Column {
                                            Text(
                                                text = "Nombre",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AppTextPrimary.copy(alpha = 0.7f),
                                                fontSize = 12.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = currentUser.name,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = AppTextPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            // Stats Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = androidx.compose.ui.graphics.Color(0xFF3A4550)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp)
                                ) {
                                    Text(
                                        text = "Estadísticas",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = AppAccent,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Points
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "Puntos",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AppTextPrimary.copy(alpha = 0.7f),
                                                fontSize = 12.sp
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "${currentUser.points}",
                                                style = MaterialTheme.typography.headlineLarge,
                                                color = AppAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "pts",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AppTextPrimary.copy(alpha = 0.7f),
                                                fontSize = 10.sp
                                            )
                                        }

                                        // Divider
                                        Divider(
                                            modifier = Modifier
                                                .height(60.dp)
                                                .width(1.dp),
                                            color = AppTextPrimary.copy(alpha = 0.3f)
                                        )

                                        // Streak
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "Racha",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AppTextPrimary.copy(alpha = 0.7f),
                                                fontSize = 12.sp
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = "Streak",
                                                    tint = AppYellow,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Text(
                                                    text = "${currentUser.streak}",
                                                    style = MaterialTheme.typography.headlineLarge,
                                                    color = AppAccent,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Text(
                                                text = "días",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AppTextPrimary.copy(alpha = 0.7f),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(78.dp))
                            Button(
                                onClick = {
                                    authRepo.signOut()
                                    navController.navigate(Routes.Login.route) {
                                        popUpTo(Routes.Home.route) { inclusive = true }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = androidx.compose.ui.graphics.Color(0xFF8A2727) // Red color
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Cerrar Sesión",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                            }
                        }

                        // Sign Out Button
                    }
                }
            }
        }
    }
}

