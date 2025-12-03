package com.example.firebase_lsm_pp.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebase_lsm_pp.auth.AppUser
import com.example.firebase_lsm_pp.auth.AuthRepository
import com.example.firebase_lsm_pp.auth.FirestoreUserService
import com.example.firebase_lsm_pp.ui.theme.AppAccent
import com.example.firebase_lsm_pp.ui.theme.AppBackground
import com.example.firebase_lsm_pp.ui.theme.AppButtonColor
import com.example.firebase_lsm_pp.ui.theme.AppTextPrimary
import com.example.firebase_lsm_pp.ui.theme.AppYellow

@Composable
fun LeaderboardScreen() {
    var topUsers by remember { mutableStateOf<List<AppUser>>(emptyList()) }
    var currentUser by remember { mutableStateOf<AppUser?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val userService = remember { FirestoreUserService() }
    val authRepo = remember { AuthRepository() }

    LaunchedEffect(Unit) {
        try {
            // Get top users
            topUsers = userService.getTopUsers(limit = 20)

            // Get current user
            val currentUserUid = authRepo.getCurrentUser()?.uid
            if (currentUserUid != null) {
                currentUser = userService.getUser(currentUserUid)
            }

            loading = false
        } catch (e: Exception) {
            error = "Error al cargar el leaderboard: ${e.message}"
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
            // Header
            Text(
                text = "Leaderboard",
                style = MaterialTheme.typography.headlineLarge,
                color = AppAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
            )

            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppAccent)
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(topUsers) { index, user ->
                            LeaderboardItem(
                                user = user,
                                rank = index + 1,
                                isCurrentUser = currentUser?.uid == user.uid
                            )
                        }
                    }

                    // Current User Card at the bottom
                    currentUser?.let { user ->
                        CurrentUserCard(
                            user = user,
                            rank = topUsers.indexOfFirst { it.uid == user.uid } + 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardItem(
    user: AppUser,
    rank: Int,
    isCurrentUser: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) {
                AppButtonColor.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (rank <= 3) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Rank and Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rank Badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when (rank) {
                                1 -> AppYellow
                                2 -> androidx.compose.ui.graphics.Color(0xFFC0C0C0) // Silver
                                3 -> androidx.compose.ui.graphics.Color(0xFFCD7F32) // Bronze
                                else -> AppButtonColor.copy(alpha = 0.5f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$rank",
                        color = if (rank <= 3) androidx.compose.ui.graphics.Color.Black else AppTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Username
                Column {
                    Text(
                        text = user.username.ifEmpty { user.name.ifEmpty { "Usuario" } },
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (user.name.isNotEmpty() && user.name != user.username) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTextPrimary.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Points and Streak
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Streak",
                        tint = AppYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${user.streak}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Points
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${user.points}",
                        style = MaterialTheme.typography.titleLarge,
                        color = AppAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "pts",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTextPrimary.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CurrentUserCard(
    user: AppUser,
    rank: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        color = AppButtonColor.copy(alpha = 0.2f),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Tu posición",
                style = MaterialTheme.typography.titleMedium,
                color = AppAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Rango",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTextPrimary.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = if (rank > 0) "#$rank" else "N/A",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AppAccent,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Points
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Puntos",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTextPrimary.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "${user.points}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AppAccent,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Streak
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Racha",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTextPrimary.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Streak",
                            tint = AppYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${user.streak}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = AppAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}