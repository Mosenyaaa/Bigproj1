package com.example.bigproj.presentation.components

import android.Manifest
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.bigproj.domain.repository.FileRepository
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    var isUploading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Отслеживаем статус разрешения
    LaunchedEffect(recordPermissionState.status) {
        if (recordPermissionState.status.isGranted && !isRecording) {
            try {
                outputFile = startRecording(context)
                isRecording = true
                errorMessage = null
                println("🎤 Разрешение получено, начинаем запись автоматически")
            } catch (e: Exception) {
                errorMessage = "Ошибка начала записи: ${e.message}"
                println("❌ Ошибка после получения разрешения: ${e.message}")
            }
        }
    }

    // Показываем индикатор загрузки
    if (isUploading) {
        Dialog(onDismissRequest = {}) {
            Card(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Загрузка аудио на сервер...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Пожалуйста, подождите",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
        return
    }

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
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isRecording) {
                    // Индикатор записи
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Визуализатор звука
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            repeat(10) { index ->
                                val height = if ((recordingTime + index) % 3 == 0) {
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
                            text = formatTime(recordingTime),
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { (recordingTime % 60).toFloat() / 60f },
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF4CAF50),
                            trackColor = Color(0xFFE0E0E0)
                        )
                    }
                } else {
                    Text(
                        text = if (recordPermissionState.status.isGranted)
                            "Нажмите кнопку чтобы начать запись"
                        else "Нужно разрешение на запись аудио",
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
                                println("🎯 Кнопка 'Начать запись' нажата")
                                println("📋 Статус разрешения: ${recordPermissionState.status}")

                                if (recordPermissionState.status.isGranted) {
                                    try {
                                        outputFile = startRecording(context)
                                        isRecording = true
                                        errorMessage = null
                                        println("✅ Запись начата вручную")
                                    } catch (e: Exception) {
                                        errorMessage = "Ошибка начала записи: ${e.message}"
                                        println("❌ Ошибка начала записи: ${e.message}")
                                    }
                                } else {
                                    println("📝 Запрашиваем разрешение...")
                                    recordPermissionState.launchPermissionRequest()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text(
                                if (recordPermissionState.status.isGranted)
                                    "Начать запись"
                                else "Запросить разрешение"
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                println("⏹️ Кнопка 'Остановить' нажата")
                                isRecording = false
                                stopRecording(mediaRecorder)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red
                            )
                        ) {
                            Text("Остановить")
                        }
                    }

                    Button(
                        onClick = {
                            println("❌ Кнопка 'Отмена' нажата")
                            onDismiss()
                        },
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

    // Обработка остановки записи
    LaunchedEffect(isRecording) {
        if (!isRecording && outputFile != null && outputFile!!.exists()) {
            val localContext = context
            val localOutputFile = outputFile!!

            try {
                println("🎙️ Запись завершена, начинаем загрузку файла...")
                println("📁 Локальный файл: ${localOutputFile.absolutePath}")
                println("📏 Размер файла: ${localOutputFile.length()} байт")

                // Конвертируем File в Uri
                val fileUri = Uri.fromFile(localOutputFile)

                // ЗАГРУЗКА НА СЕРВЕР
                println("📤 Пытаемся загрузить файл на сервер...")

                // Создаем FileRepository
                val fileRepository = FileRepository(localContext)

                // Пытаемся загрузить
                val serverFilename = try {
                    fileRepository.uploadVoiceFile(fileUri) // ← ИМЯ МЕТОДА ИЗМЕНИЛОСЬ!
                } catch (e: Exception) {
                    println("❌ Ошибка загрузки: ${e.message}")
                    "audio_${System.currentTimeMillis()}.mp3"
                }

                println("📥 Результат загрузки: $serverFilename")

                // Возвращаем результат
                onRecordingComplete(serverFilename)
                onDismiss()

            } catch (e: Exception) {
                println("❌ Ошибка при обработке записи: ${e.message}")
                e.printStackTrace()

                // Возвращаем локальный путь как fallback
                onRecordingComplete(localOutputFile.absolutePath)
                onDismiss()
            } finally {
                outputFile = null
            }
        }
    }

    // Таймер записи
    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1000)
            recordingTime++
            if (recordingTime % 5 == 0) {
                println("⏱️ Запись идет: $recordingTime сек")
            }
        }
    }

    // Инициализация MediaRecorder
    LaunchedEffect(isRecording) {
        if (isRecording && outputFile != null) {
            try {
                mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }

                mediaRecorder?.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(outputFile!!.absolutePath)
                    prepare()
                    start()

                    println("🎤 MediaRecorder запущен успешно!")
                    println("📁 Запись в файл: ${outputFile!!.absolutePath}")
                }
            } catch (e: Exception) {
                errorMessage = "Ошибка записи: ${e.message}"
                println("❌ Ошибка инициализации MediaRecorder: ${e.message}")
                e.printStackTrace()
                isRecording = false
            }
        }
    }

    // Очистка при закрытии
    DisposableEffect(Unit) {
        onDispose {
            println("🧹 Очистка VoiceRecorderDialog")
            if (isRecording) {
                println("⏹️ Останавливаем запись при закрытии")
                stopRecording(mediaRecorder)
            }
            mediaRecorder?.release()
            println("✅ MediaRecorder освобожден")
        }
    }
}

// Вспомогательная функция для начала записи
private fun startRecording(context: android.content.Context): File {
    // Создаем файл для записи
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.getExternalFilesDir(null)
    val file = File(storageDir, "voice_$timeStamp.mp3")

    println("🎤 Начинаем запись в файл: ${file.absolutePath}")
    println("📂 Директория: ${storageDir?.absolutePath}")
    return file
}

// Вспомогательная функция для остановки записи
private fun stopRecording(mediaRecorder: MediaRecorder?) {
    try {
        mediaRecorder?.apply {
            stop()
            release()
        }
        println("⏹️ Запись остановлена успешно")
    } catch (e: Exception) {
        println("❌ Ошибка остановки записи: ${e.message}")
        e.printStackTrace()
    }
}

// Вспомогательная функция для форматирования времени
private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}