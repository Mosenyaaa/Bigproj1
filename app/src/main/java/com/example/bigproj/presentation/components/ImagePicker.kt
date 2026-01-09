// presentation/components/ImagePicker.kt
package com.example.bigproj.presentation.components

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ImagePickerDialog(
    onImageSelected: (String) -> Unit, // Возвращает имя файла на сервере
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val storagePermissionState = rememberPermissionState(
        permission = Manifest.permission.READ_EXTERNAL_STORAGE
    )

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) } // Индикатор загрузки

    // Запускатель для выбора изображения
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedImageUri = uri
            }
        }
    )

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
                        text = "Загрузка изображения на сервер...",
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
        return // Не показываем основной диалог пока идет загрузка
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🖼️ Выбор изображения",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedImageUri != null) {
                    // Превью выбранного изображения
                    ImagePreview(
                        imageUri = selectedImageUri!!,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(vertical = 16.dp)
                    )

                    // Кнопка загрузки (неактивна, загрузка идет автоматически)
                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        enabled = false
                    ) {
                        Text("Загрузка начата...")
                    }
                } else {
                    Text(
                        text = "Выберите изображение для вопроса",
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
                    if (selectedImageUri == null) {
                        Button(
                            onClick = {
                                if (storagePermissionState.status.isGranted) {
                                    pickImageLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                } else {
                                    storagePermissionState.launchPermissionRequest()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Text("Выбрать изображение")
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

    // Обработка выбранного изображения
    LaunchedEffect(selectedImageUri) {
        selectedImageUri?.let { uri ->
            isUploading = true
            errorMessage = null

            try {
                println("🖼️ Изображение выбрано, начинаем загрузку...")
                println("📁 URI изображения: $uri")

                // Загружаем файл на сервер
                val fileRepository = com.example.bigproj.domain.repository.FileRepository(context)
                val serverFilename = fileRepository.uploadImageFile(uri)

                println("✅ Изображение загружено на сервер: $serverFilename")

                // Возвращаем имя файла с сервера
                onImageSelected(serverFilename)
                onDismiss()

            } catch (e: Exception) {
                println("❌ Ошибка загрузки изображения: ${e.message}")
                errorMessage = "Ошибка загрузки: ${e.message}"
                e.printStackTrace()
            } finally {
                isUploading = false
            }
        }
    }
}

@Composable
fun ImagePreview(
    imageUri: Uri,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUri,
            contentDescription = "Выбранное изображение",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}