package com.example.bigproj.presentation.components

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceRecorderDialog(
    onRecordingComplete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val recordPermissionState = rememberPermissionState(
        permission = Manifest.permission.RECORD_AUDIO
    )

    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf(0) }
    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
    var outputFile: File? by remember { mutableStateOf(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isRecording) "🎤 Запись..." else "🎤 Запись голоса",
                    fontSize = 20.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isRecording) {
                    // Индикатор записи
                    RecordingIndicator(
                        timeSeconds = recordingTime,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    Text(
                        text = "Нажмите кнопку ниже чтобы начать запись",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isRecording) {
                        Button(
                            onClick = {
                                if (recordPermissionState.status.isGranted) {
                                    try {
                                        outputFile = startRecording(context)
                                        isRecording = true
                                        errorMessage = null
                                    } catch (e: Exception) {
                                        errorMessage = "Ошибка начала записи: ${e.message}"
                                    }
                                } else {
                                    recordPermissionState.launchPermissionRequest()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text("Начать запись")
                        }
                    } else {
                        Button(
                            onClick = {
                                isRecording = false
                                stopRecording(mediaRecorder)
                                if (outputFile != null && outputFile!!.exists()) {
                                    onRecordingComplete(outputFile!!.absolutePath)
                                }
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red
                            )
                        ) {
                            Text("Остановить")
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF666666)
                        )
                    ) {
                        Text("Отмена")
                    }
                }
            }
        }
    }

    // Таймер записи
    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1000)
            recordingTime++
        }
    }

    // Инициализация MediaRecorder
    LaunchedEffect(isRecording) {
        if (isRecording && outputFile != null) {
            try {
                mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    MediaRecorder()
                }

                mediaRecorder?.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(outputFile!!.absolutePath)
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                errorMessage = "Ошибка записи: ${e.message}"
                isRecording = false
            }
        }
    }

    // Очистка при закрытии
    DisposableEffect(Unit) {
        onDispose {
            if (isRecording) {
                stopRecording(mediaRecorder)
            }
            mediaRecorder?.release()
        }
    }
}

@Composable
fun RecordingIndicator(
    timeSeconds: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Визуализатор звука
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(10) { index ->
                val height = if ((timeSeconds + index) % 3 == 0) {
                    (20 + (index * 3)).dp
                } else {
                    (10 + (index * 2)).dp
                }
                Box(
                    modifier = Modifier
                        .size(4.dp, height)
                        .background(
                            Color(0xFF4CAF50),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = formatTime(timeSeconds),
            fontSize = 14.sp,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = (timeSeconds % 60).toFloat() / 60f,
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF4CAF50),
            trackColor = Color(0xFFE0E0E0)
        )
    }
}

// Вспомогательные функции
private fun startRecording(context: Context): File {
    // Создаем файл для записи
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(null)
    return File(storageDir, "voice_$timeStamp.mp3")
}

private fun stopRecording(mediaRecorder: MediaRecorder?) {
    try {
        mediaRecorder?.apply {
            stop()
            release()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}