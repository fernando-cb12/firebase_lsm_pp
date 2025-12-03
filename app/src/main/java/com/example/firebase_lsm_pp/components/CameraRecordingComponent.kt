package com.example.firebase_lsm_pp.components

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.AudioConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.firebase_lsm_pp.services.SignEvaluationService
import com.example.firebase_lsm_pp.ui.theme.AppBackground
import com.example.firebase_lsm_pp.ui.theme.AppButtonColor
import com.example.firebase_lsm_pp.ui.theme.AppMainText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CameraRecordingDialog(
    onDismiss: () -> Unit,
    onVideoSaved: (File) -> Unit = {}
) {
    val context = LocalContext.current
    
    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { perms ->
            hasPermissions = perms.values.all { it }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        if (hasPermissions) {
            CameraContent(
                onDismiss = onDismiss,
                onVideoSaved = onVideoSaved
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppBackground),
                contentAlignment = Alignment.Center
            ) {
                Text("Se requieren permisos de cámara y audio", color = AppMainText)
            }
        }
    }
}

@Composable
fun CameraContent(
    onDismiss: () -> Unit,
    onVideoSaved: (File) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }
    val signService = remember { SignEvaluationService() }
    val scope = rememberCoroutineScope()

    var isRecording by remember { mutableStateOf(false) }
    var recordingState by remember { mutableStateOf<RecordingState>(RecordingState.Idle) }
    var recording: Recording? by remember { mutableStateOf(null) }
    var countdown by remember { mutableStateOf(0) }
    var recordingTime by remember { mutableStateOf(0) }
    var recordedVideoFile by remember { mutableStateOf<File?>(null) }
    var feedbackHints by remember { mutableStateOf<List<String>>(emptyList()) }

    // Configurar cámara
    LaunchedEffect(Unit) {
        cameraController.bindToLifecycle(lifecycleOwner)
        cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        cameraController.setEnabledUseCases(LifecycleCameraController.VIDEO_CAPTURE)
    }

    // Lógica de grabación
    fun startRecordingFlow() {
        recordingState = RecordingState.Countdown
    }

    // Efecto para la cuenta regresiva
    LaunchedEffect(recordingState) {
        if (recordingState == RecordingState.Countdown) {
            for (i in 2 downTo 1) {
                countdown = i
                delay(1000)
            }
            recordingState = RecordingState.Recording
        }
    }

    // Efecto para iniciar grabación y temporizador de 5s
    LaunchedEffect(recordingState) {
        if (recordingState == RecordingState.Recording) {
            val videoFile = File(
                context.filesDir,
                "recording_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}.mp4"
            )
            
            val outputOptions = FileOutputOptions.Builder(videoFile).build()
            
            recording = cameraController.startRecording(
                outputOptions,
                AudioConfig.create(true),
                ContextCompat.getMainExecutor(context)
            ) { event ->
                if (event is VideoRecordEvent.Finalize) {
                    if (!event.hasError()) {
                        recordedVideoFile = videoFile
                        onVideoSaved(videoFile)
                    } else {
                        recording?.close()
                        recording = null
                        Toast.makeText(context, "Error al grabar", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            isRecording = true

            // Timer de 5 segundos
            for (i in 0..5) {
                recordingTime = i
                if (i < 5) delay(1000)
            }
            
            // Detener grabación
            recording?.stop()
            recording = null
            isRecording = false
            recordingState = RecordingState.Finished
        }
    }

    // Efecto para enviar video cuando cambia el estado
    LaunchedEffect(recordingState) {
        if (recordingState == RecordingState.Sending) {
            recordedVideoFile?.let { file ->
                val result = signService.evaluateSign(file)
                result.onSuccess { response ->
                    feedbackHints = response.hints
                    recordingState = RecordingState.Feedback
                }.onFailure {
                    Toast.makeText(context, "Error al evaluar seña", Toast.LENGTH_SHORT).show()
                    recordingState = RecordingState.Finished
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Vista de cámara
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    controller = cameraController
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header (Botón cerrar si no está grabando)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (recordingState == RecordingState.Idle || recordingState == RecordingState.Finished || recordingState == RecordingState.Feedback) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.5f))
                    ) {
                        Text("Cerrar", color = Color.White)
                    }
                }
            }

            // Center Content (Countdown or Status)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (recordingState) {
                    RecordingState.Countdown -> {
                        Text(
                            text = countdown.toString(),
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    RecordingState.Recording -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Grabando...",
                                fontSize = 24.sp,
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${recordingTime}s / 5s",
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    }
                    RecordingState.Sending -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Enviando video...",
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }
                    }
                    RecordingState.Feedback -> {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = AppBackground)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Sugerencias",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = AppMainText,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(feedbackHints) { hint ->
                                        Row(verticalAlignment = Alignment.Top) {
                                            Text("• ", color = AppMainText, fontSize = 16.sp)
                                            Text(hint, color = AppMainText, fontSize = 16.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }

            // Footer Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (recordingState) {
                    RecordingState.Idle -> {
                        Button(
                            onClick = { startRecordingFlow() },
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Grabar", color = Color.White)
                    }
                    RecordingState.Finished -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { startRecordingFlow() },
                                colors = ButtonDefaults.buttonColors(containerColor = AppButtonColor)
                            ) {
                                Text("Re-grabar", color = AppMainText)
                            }
                            Button(
                                onClick = { recordingState = RecordingState.Sending },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                            ) {
                                Text("Enviar", color = Color.White)
                            }
                        }
                    }
                    RecordingState.Feedback -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { startRecordingFlow() },
                                colors = ButtonDefaults.buttonColors(containerColor = AppButtonColor)
                            ) {
                                Text("Re-intentar", color = AppMainText)
                            }
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                            ) {
                                Text("Cerrar", color = Color.White)
                            }
                        }
                    }
                    else -> {}
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

enum class RecordingState {
    Idle,
    Countdown,
    Recording,
    Finished,
    Sending,
    Feedback
}
