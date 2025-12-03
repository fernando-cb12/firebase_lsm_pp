package com.example.firebase_lsm_pp.screens

import android.net.Uri
import android.view.ViewGroup
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.firebase_lsm_pp.models.Sign
import com.example.firebase_lsm_pp.services.FirestoreSignService
import com.example.firebase_lsm_pp.ui.theme.AppAccent
import com.example.firebase_lsm_pp.ui.theme.AppBackground
import com.example.firebase_lsm_pp.ui.theme.AppButtonColor
import com.example.firebase_lsm_pp.ui.theme.AppMainText

@Composable
fun DictionaryScreen() {
    var signs by remember { mutableStateOf<List<Sign>>(emptyList()) }
    var filteredSigns by remember { mutableStateOf<List<Sign>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todas") }
    var selectedSign by remember { mutableStateOf<Sign?>(null) }

    val signService = remember { FirestoreSignService() }

    // Categorías disponibles - se cargan dinámicamente desde las señas
    val categories = remember(signs) {
        val uniqueCategories = signs.map { it.category }.distinct().filter { it.isNotBlank() }.sorted()
        listOf("Todas") + uniqueCategories
    }

    // Cargar señas desde Firebase
    LaunchedEffect(Unit) {
        try {
            signs = signService.getAllSigns()
            filteredSigns = signs
            loading = false
        } catch (e: Exception) {
            loading = false
        }
    }

    // Filtrar por categoría y búsqueda
    LaunchedEffect(searchQuery, selectedCategory, signs) {
        val categoryFiltered = if (selectedCategory == "Todas") {
            signs
        } else {
            signs.filter { it.category == selectedCategory }
        }

        filteredSigns = if (searchQuery.isBlank()) {
            categoryFiltered
        } else {
            categoryFiltered.filter {
                it.word.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Título
        Text(
            text = "Señas",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
            color = AppMainText
        )

        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = {
                Text(
                    text = "Buscar señas...",
                    color = Color.Gray
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = Color.Gray
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = AppButtonColor,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            singleLine = true
        )

        // Filtros de categoría
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = {
                        Text(
                            text = category,
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AppButtonColor,
                        selectedLabelColor = AppMainText,
                        containerColor = AppAccent,
                        labelColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // Grid de señas
        if (loading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (filteredSigns.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotBlank())
                        "No se encontraron señas"
                    else
                        "No hay señas disponibles",
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppMainText
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredSigns) { sign ->
                    SignButton(
                        sign = sign,
                        onClick = { selectedSign = sign }
                    )
                }
            }
        }
    }

    // Mostrar modal cuando hay una seña seleccionada
    selectedSign?.let { sign ->
        SignDetailDialog(
            sign = sign,
            onDismiss = { selectedSign = null }
        )
    }
}

@Composable
fun SignButton(sign: Sign, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppAccent,
            contentColor = Color.Black
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp
        )
    ) {
        Text(
            text = sign.word,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SignDetailDialog(
    sign: Sign,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppBackground
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Botón de cerrar (X) en la esquina superior derecha
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = AppMainText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Video
                if (sign.videoURL.isNotBlank()) {
                    VideoPlayer(
                        videoUrl = sign.videoURL,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(AppAccent, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Video no disponible",
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nombre de la seña
                Text(
                    text = sign.word,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AppMainText,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Descripción
                Text(
                    text = sign.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AppMainText,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón "Entendido"
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppButtonColor
                    )
                ) {
                    Text(
                        text = "Entendido",
                        color = AppMainText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun VideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    AndroidView(
        modifier = modifier,
        factory = {
            VideoView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setOnPreparedListener { mp ->
                    mp.isLooping = true
                }
                setVideoURI(Uri.parse(videoUrl))
                start()
            }
        },
        update = {
            it.setVideoURI(Uri.parse(videoUrl))
            it.start()
        }
    )
}
