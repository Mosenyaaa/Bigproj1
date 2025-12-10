// presentation/Screen/QuestionEditorScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay
import java.io.File

@OptIn(ExperimentalMaterial3Api::class) // Добавляем аннотацию для экспериментального API
@Composable
fun QuestionEditorScreen(
    questionIndex: Int,
    onBackClick: () -> Unit = {},
) {
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

    var voiceRecordingResult by remember { mutableStateOf<String?>(null) }
    var imageSelectionResult by remember { mutableStateOf<android.net.Uri?>(null) }

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
                    TextButton(
                        onClick = onBackClick,
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
                    viewModel.onEvent(SurveyManagementEvent.RemoveQuestionVoice)
                },
                onRemoveImageFile = {
                    viewModel.onEvent(SurveyManagementEvent.RemoveQuestionImage)
                },
                onStartVoiceRecording = { showVoiceRecorder = true },
                onStartImageSelection = { showImagePicker = true },
                modifier = Modifier.padding(paddingValues)
            )

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
    onRemoveVoiceFile: () -> Unit,
    onRemoveImageFile: () -> Unit,
    onStartVoiceRecording: () -> Unit,
    onStartImageSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Информация о типе вопроса
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (question.type) {
                    "text" -> Color(0xFFE8F5E9)
                    "voice" -> Color(0xFFE3F2FD)
                    "picture" -> Color(0xFFF3E5F5)
                    "combined" -> Color(0xFFFFF3E0)
                    else -> Color(0xFFF5F5F5)
                }
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = when (question.type) {
                        "text" -> "📝 Текстовый вопрос"
                        "voice" -> "🎤 Голосовой вопрос"
                        "picture" -> "🖼️ Вопрос с изображением"
                        "combined" -> "🔗 Комбинированный вопрос"
                        else -> "❓ Неизвестный тип"
                    },
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when (question.type) {
                        "text" -> "Введите текст вопроса"
                        "voice" -> "Загрузите аудиофайл. Текст - необязательно"
                        "picture" -> "Загрузите изображение. Текст - необязательно"
                        "combined" -> "Загрузите и аудио, и изображение"
                        else -> ""
                    },
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Загрузка файла...", fontSize = 12.sp, color = Color(0xFF666666))
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MediaButton(
                    text = if (question.voiceFilename != null) "Голос загружен" else "Записать голос",
                    isActive = question.voiceFilename != null,
                    indicatorColor = Color(0xFF4CAF50),
                    onClick = onStartVoiceRecording,
                    modifier = Modifier.weight(1f)
                )

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
            Text(text = uploadError, color = Color.Red, fontSize = 12.sp)
        }

        // Информация о загруженных файлах
        if (question.voiceFilename != null || question.pictureFilename != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
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
                            Column(modifier = Modifier.weight(1f)) {
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
                            Button(
                                onClick = onRemoveVoiceFile,
                                modifier = Modifier.size(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                )
                            ) {
                                Text("×", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
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
                            Column(modifier = Modifier.weight(1f)) {
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
                            Button(
                                onClick = onRemoveImageFile,
                                modifier = Modifier.size(24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                )
                            ) {
                                Text("×", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            ) {
                Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (question.answerOptions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(question.answerOptions) { index, option ->
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
            containerColor = if (isActive) indicatorColor.copy(alpha = 0.1f) else Color.White,
            contentColor = if (isActive) indicatorColor else Color.Black
        ),
        border = if (!isActive) BorderStroke(1.dp, Color(0xFFE0E0E0)) else null
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Вместо иконки - цветной кружок с буквой
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = text, fontSize = 12.sp, textAlign = TextAlign.Center)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(0xFFE3F2FD), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(text = text, fontSize = 14.sp, color = Color(0xFF444444))
            }

            Button(
                onClick = onRemove,
                modifier = Modifier.size(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("×", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}