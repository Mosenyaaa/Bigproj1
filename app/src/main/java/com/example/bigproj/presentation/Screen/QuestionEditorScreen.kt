package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bigproj.domain.repository.FileRepository
import com.example.bigproj.presentation.Screen.state.SurveyManagementEvent
import com.example.bigproj.presentation.Screen.viewmodel.SurveyManagementViewModel
import com.example.bigproj.presentation.components.ImagePickerDialog
import com.example.bigproj.presentation.components.VoiceRecorderDialog
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionEditorScreen(
    questionIndex: Int,
    onBackClick: () -> Unit = {},
) {
    // Получаем ViewModel внутри композабла
    val viewModel: SurveyManagementViewModel = viewModel()
    val context = LocalContext.current
    val state = viewModel.state

    val question = if (questionIndex in state.questions.indices) {
        state.questions[questionIndex]
    } else {
        onBackClick()
        return
    }

    val fileRepository = remember { FileRepository(context) }

    var newAnswerOption by remember { mutableStateOf("") }
    var showVoiceRecorder by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    // Переменные для хранения результата из диалогов
    var voiceRecordingResult by remember { mutableStateOf<String?>(null) }
    var imageSelectionResult by remember { mutableStateOf<android.net.Uri?>(null) }

    // Обработка результата записи голоса
    LaunchedEffect(voiceRecordingResult) {
        voiceRecordingResult?.let { filePath ->
            try {
                isUploading = true
                uploadError = null
                val uri = android.net.Uri.fromFile(File(filePath))
                val filename = fileRepository.uploadVoiceFile(uri)
                viewModel.onEvent(SurveyManagementEvent.UpdateQuestionVoiceFile(filename))
                isUploading = false
                showVoiceRecorder = false
            } catch (e: Exception) {
                uploadError = "Ошибка загрузки голоса: ${e.message}"
                isUploading = false
            } finally {
                voiceRecordingResult = null
            }
        }
    }

    // Обработка результата выбора изображения
    LaunchedEffect(imageSelectionResult) {
        imageSelectionResult?.let { uri ->
            try {
                isUploading = true
                uploadError = null
                val filename = fileRepository.uploadImageFile(uri)
                viewModel.onEvent(SurveyManagementEvent.UpdateQuestionImageFile(filename))
                isUploading = false
                showImagePicker = false
            } catch (e: Exception) {
                uploadError = "Ошибка загрузки изображения: ${e.message}"
                isUploading = false
            } finally {
                imageSelectionResult = null
            }
        }
    }

    // Сброс состояний при закрытии диалогов
    LaunchedEffect(showVoiceRecorder) {
        if (!showVoiceRecorder) {
            isUploading = false
            uploadError = null
        }
    }

    LaunchedEffect(showImagePicker) {
        if (!showImagePicker) {
            isUploading = false
            uploadError = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактирование вопроса ${questionIndex + 1}") },
                navigationIcon = {
                    Button(
                        onClick = onBackClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("← Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box {
            QuestionEditorContent(
                question = question,
                newAnswerOption = newAnswerOption,
                isUploading = isUploading,
                uploadError = uploadError,
                onNewAnswerOptionChange = { newAnswerOption = it },
                onAddAnswerOption = {
                    if (it.isNotBlank()) {
                        viewModel.onEvent(SurveyManagementEvent.AddAnswerOption(it))
                        newAnswerOption = ""
                    }
                },
                onRemoveAnswerOption = { index ->
                    viewModel.onEvent(SurveyManagementEvent.RemoveAnswerOption(index))
                },
                onTextChange = { text ->
                    viewModel.onEvent(SurveyManagementEvent.UpdateQuestionText(text))
                },
                onRemoveVoiceFile = {
                    viewModel.onEvent(SurveyManagementEvent.UpdateQuestionVoiceFile(null))
                },
                onRemoveImageFile = {
                    viewModel.onEvent(SurveyManagementEvent.UpdateQuestionImageFile(null))
                },
                onStartVoiceRecording = { showVoiceRecorder = true },
                onStartImageSelection = { showImagePicker = true },
                modifier = Modifier.padding(paddingValues)
            )

            // Диалог записи голоса
            if (showVoiceRecorder) {
                VoiceRecorderDialog(
                    onRecordingComplete = { filePath ->
                        voiceRecordingResult = filePath
                    },
                    onDismiss = {
                        showVoiceRecorder = false
                        voiceRecordingResult = null
                    }
                )
            }

            // Диалог выбора изображения
            if (showImagePicker) {
                ImagePickerDialog(
                    onImageSelected = { uri ->
                        imageSelectionResult = uri
                    },
                    onDismiss = {
                        showImagePicker = false
                        imageSelectionResult = null
                    }
                )
            }
        }
    }
}

@Composable
fun QuestionEditorContent(
    question: com.example.bigproj.presentation.Screen.state.QuestionUiModel,
    newAnswerOption: String,
    isUploading: Boolean,
    uploadError: String?,
    onNewAnswerOptionChange: (String) -> Unit,
    onAddAnswerOption: (String) -> Unit,
    onRemoveAnswerOption: (Int) -> Unit,
    onTextChange: (String) -> Unit,
    onRemoveVoiceFile: () -> Unit, // ← ДОБАВЛЕНО
    onRemoveImageFile: () -> Unit, // ← ДОБАВЛЕНО
    onStartVoiceRecording: () -> Unit,
    onStartImageSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Тип вопроса (информация)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when (question.type) {
                                "text" -> Color(0xFF4CAF50)
                                "voice" -> Color(0xFF2196F3)
                                "picture" -> Color(0xFF9C27B0)
                                "combined" -> Color(0xFFFF9800)
                                else -> Color(0xFF666666)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (question.type) {
                            "text" -> "T"
                            "voice" -> "V"
                            "picture" -> "I"
                            "combined" -> "C"
                            else -> "?"
                        },
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = question.displayType,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Тип определён автоматически",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Текст вопроса
        Text(
            text = "Текст вопроса",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = question.text,
            onValueChange = onTextChange,
            placeholder = { Text("Введите текст вопроса...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            singleLine = false
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Медиа-файлы
        Text(
            text = "Медиа-файлы",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Загрузка файла...",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Кнопка записи голоса
                MediaButton(
                    text = if (question.voiceFilename != null) "Голос загружен" else "Записать голос",
                    isActive = question.voiceFilename != null,
                    indicatorColor = Color(0xFF4CAF50),
                    onClick = onStartVoiceRecording,
                    modifier = Modifier.weight(1f)
                )

                // Кнопка выбора изображения
                MediaButton(
                    text = if (question.pictureFilename != null) "Изображение загружено" else "Добавить изображение",
                    isActive = question.pictureFilename != null,
                    indicatorColor = Color(0xFF2196F3),
                    onClick = onStartImageSelection,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (uploadError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uploadError,
                color = Color.Red,
                fontSize = 12.sp
            )
        }

        // Информация о загруженных файлах
        if (question.voiceFilename != null || question.pictureFilename != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    if (question.voiceFilename != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4CAF50))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Голосовой файл",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF444444)
                                )
                                Text(
                                    text = question.voiceFilename!!,
                                    fontSize = 10.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                            // Кнопка удаления голосового файла
                            Button(
                                onClick = onRemoveVoiceFile,
                                modifier = Modifier.size(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color.Red
                                )
                            ) {
                                Text("×", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (question.pictureFilename != null) {
                        if (question.voiceFilename != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2196F3))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Изображение",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF444444)
                                )
                                Text(
                                    text = question.pictureFilename!!,
                                    fontSize = 10.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                            // Кнопка удаления изображения
                            Button(
                                onClick = onRemoveImageFile,
                                modifier = Modifier.size(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color.Red
                                )
                            ) {
                                Text("×", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Варианты ответов (для вопросов с выбором)
        Text(
            text = "Варианты ответов",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Добавьте варианты ответов, если вопрос предполагает выбор",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Добавление нового варианта
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newAnswerOption,
                onValueChange = onNewAnswerOptionChange,
                placeholder = { Text("Новый вариант ответа") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = { onAddAnswerOption(newAnswerOption) },
                enabled = newAnswerOption.isNotBlank(),
                modifier = Modifier.size(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Список вариантов
        if (!question.answerOptions.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(question.answerOptions!!) { index, option ->
                    AnswerOptionItem(
                        text = option,
                        index = index,
                        onRemove = { onRemoveAnswerOption(index) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}


@Composable
fun MediaButton(
    text: String,
    isActive: Boolean,
    indicatorColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) {
                indicatorColor.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            },
            contentColor = if (isActive) {
                indicatorColor
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ),
        border = if (!isActive) {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                Color(0xFFE0E0E0)
            )
        } else null
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AnswerOptionItem(
    text: String,
    index: Int,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = text,
                    fontSize = 14.sp,
                    color = Color(0xFF444444)
                )
            }

            Button(
                onClick = onRemove,
                modifier = Modifier.size(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF666666)
                )
            ) {
                Text("×", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}