package com.example.firebase_lsm_pp.screens

import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.firebase_lsm_pp.models.Lesson
import com.example.firebase_lsm_pp.services.FirestoreLessonService
import com.example.firebase_lsm_pp.ui.theme.AppAccent
import com.example.firebase_lsm_pp.ui.theme.AppBackground
import com.example.firebase_lsm_pp.ui.theme.AppButtonColor
import coil.compose.AsyncImage

@Composable
fun LessonDetailScreen(
    navController: NavController,
    lessonTitle: String
) {
    var lesson by remember { mutableStateOf<Lesson?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var showVideoDialog by remember { mutableStateOf<String?>(null) }
    var showResultDialog by remember { mutableStateOf<Boolean?>(null) }
    var answerConfirmed by remember { mutableStateOf(false) }

    val lessonService = remember { FirestoreLessonService() }

    LaunchedEffect(lessonTitle) {
        try {
            val lessons = lessonService.getAllLessons()
            lesson = lessons.find { it.title == lessonTitle }
            if (lesson == null) {
                error = "Lección no encontrada"
            }
            loading = false
        } catch (e: Exception) {
            error = "Error al cargar la lección: ${e.message}"
            loading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        when {
            loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppAccent)
                }
            }
            error != null || lesson == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = error ?: "Lección no encontrada",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppButtonColor
                            )
                        ) {
                            Text("Volver")
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header with back button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = AppAccent
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = lesson?.title ?: "",
                            style = MaterialTheme.typography.headlineMedium,
                            color = AppAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Thumbnail
                    AsyncImage(
                        model = lesson?.thumbnail ?: "",
                        contentDescription = lesson?.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )

                    // SECTION 1: Description and Videos
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.titleLarge,
                            color = AppAccent,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = lesson?.description ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black,
                            lineHeight = androidx.compose.ui.unit.TextUnit(24f, androidx.compose.ui.unit.TextUnitType.Sp)
                        )

                        // Video Carousel
                        if (lesson?.question?.options?.isNotEmpty() == true) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Videos de la Lección",
                                style = MaterialTheme.typography.titleLarge,
                                color = AppAccent,
                                fontWeight = FontWeight.Bold
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                itemsIndexed(lesson?.question?.options ?: emptyList()) { index, option ->
                                    val videoUrl = option.video
                                    if (!videoUrl.isNullOrEmpty()) {
                                        VideoThumbnailCard(
                                            videoUrl = videoUrl,
                                            optionLabel = when (index) {
                                                0 -> "Opción A"
                                                1 -> "Opción B"
                                                2 -> "Opción C"
                                                else -> "Video ${index + 1}"
                                            },
                                            onClick = {
                                                showVideoDialog = videoUrl
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Divider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        color = AppAccent.copy(alpha = 0.3f),
                        thickness = 2.dp
                    )

                    // SECTION 2: Questions
                    if (lesson?.question?.text?.isNotEmpty() == true) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Pregunta",
                                style = MaterialTheme.typography.titleLarge,
                                color = AppAccent,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = lesson?.question?.text ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Black,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Options
                            lesson?.question?.options?.forEachIndexed { index, option ->
                                val isSelected = selectedOption == index
                                val isCorrect = option.isCorrect

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable(enabled = !answerConfirmed) {
                                            if (!answerConfirmed) {
                                                selectedOption = index
                                            }
                                        }
                                        .then(
                                            if (answerConfirmed && selectedOption == index) {
                                                Modifier.background(
                                                    when {
                                                        isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.3f)
                                                        else -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                                                    },
                                                    RoundedCornerShape(12.dp)
                                                )
                                            } else {
                                                Modifier
                                            }
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = when (index) {
                                                0 -> "A"
                                                1 -> "B"
                                                2 -> "C"
                                                else -> ""
                                            },
                                            style = MaterialTheme.typography.titleLarge,
                                            color = AppAccent,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(end = 16.dp)
                                        )
                                        Text(
                                            text = option.optionA ?: option.optionB ?: option.optionC ?: "",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.Black,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Play video button
                                        if (!answerConfirmed && option.video != null) {
                                            IconButton(
                                                onClick = {
                                                    showVideoDialog = option.video
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Ver video",
                                                    tint = AppButtonColor
                                                )
                                            }
                                        }

                                        // Result icon
                                        if (answerConfirmed && isSelected) {
                                            Icon(
                                                imageVector = if (isCorrect) {
                                                    Icons.Default.CheckCircle
                                                } else {
                                                    Icons.Default.Close
                                                },
                                                contentDescription = null,
                                                tint = if (isCorrect) {
                                                    Color(0xFF4CAF50)
                                                } else {
                                                    MaterialTheme.colorScheme.error
                                                },
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Confirm Button
                            if (selectedOption != null && !answerConfirmed) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = {
                                        answerConfirmed = true
                                        val isCorrect = lesson?.question?.options?.get(selectedOption!!)?.isCorrect == true
                                        showResultDialog = isCorrect
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppButtonColor
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "Confirmar Respuesta",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Video Dialog
    showVideoDialog?.let { videoUrl ->
        AlertDialog(
            onDismissRequest = { showVideoDialog = null },
            title = {
                Text(
                    text = "Video",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                VideoPlayer(videoUrl = videoUrl)
            },
            confirmButton = {
                TextButton(onClick = { showVideoDialog = null }) {
                    Text("Cerrar")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Result Dialog
    showResultDialog?.let { isCorrect ->
        AlertDialog(
            onDismissRequest = { showResultDialog = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = if (isCorrect) "¡Correcto!" else "Incorrecto",
                        fontWeight = FontWeight.Bold,
                        color = if (isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )
                }
            },
            text = {
                Text(
                    text = if (isCorrect) {
                        "¡Excelente! Has respondido correctamente."
                    } else {
                        "La respuesta no es correcta. Intenta de nuevo."
                    },
                    color = Color.Black
                )
            },
            confirmButton = {
                Button(
                    onClick = { showResultDialog = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCorrect) Color(0xFF4CAF50) else AppButtonColor
                    )
                ) {
                    Text("Aceptar")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun VideoThumbnailCard(
    videoUrl: String,
    optionLabel: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = AppAccent,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = optionLabel,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun VideoPlayer(videoUrl: String) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                setVideoPath(videoUrl)
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.isLooping = false
                }
                start()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}
