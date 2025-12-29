// presentation/Screen/CreateSurveyScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bigproj.presentation.Screen.state.QuestionUiModel
import com.example.bigproj.presentation.Screen.state.SurveyManagementEvent
import com.example.bigproj.presentation.Screen.viewmodel.SurveyManagementViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSurveyScreen(
    onBackClick: () -> Unit = {},
    onSurveyCreated: (Int?) -> Unit = {},
    onEditQuestion: (Int) -> Unit = {},
    externalViewModel: SurveyManagementViewModel? = null
) {
    val context = LocalContext.current
    // Используем переданный ViewModel или создаем новый
    val viewModel = externalViewModel ?: viewModel<SurveyManagementViewModel>()
    val state = viewModel.state

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.setupDependencies(context)
    }

    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = state.errorMessage!!,
                duration = SnackbarDuration.Short
            )
            delay(3000)
            viewModel.clearError()
        }
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onSurveyCreated(state.savedSurveyId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Создание опроса") },
                navigationIcon = {
                    TextButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            // FAB для добавления вопроса - сразу открываем редактор
            FloatingActionButton(
                onClick = {
                    val newIndex = state.questions.size
                    viewModel.onEvent(SurveyManagementEvent.AddNewQuestion)
                    viewModel.onEvent(SurveyManagementEvent.SelectQuestion(newIndex))
                    onEditQuestion(newIndex)
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        CreateSurveyContent(
            state = state,
            onEvent = viewModel::onEvent,
                onEditQuestion = onEditQuestion,
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun CreateSurveyContent(
    state: com.example.bigproj.presentation.Screen.state.SurveyManagementState,
    onEvent: (SurveyManagementEvent) -> Unit,
    onEditQuestion: (Int) -> Unit,
    viewModel: SurveyManagementViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Блок с информацией об опросе (по Figma: только название)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Название опроса",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = state.surveyTitle,
                    onValueChange = { onEvent(SurveyManagementEvent.UpdateSurveyTitle(it)) },
                    placeholder = { Text("Введите название опроса") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = state.surveyTitle.isBlank(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Список вопросов
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Вопросы (${state.questions.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            // Кнопка добавления вопроса (дублируем FAB для удобства)
            TextButton(
                onClick = {
                    val newIndex = state.questions.size
                    onEvent(SurveyManagementEvent.AddNewQuestion)
                    onEvent(SurveyManagementEvent.SelectQuestion(newIndex))
                    onEditQuestion(newIndex)
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("+ Добавить вопрос")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (state.questions.isEmpty()) {
            EmptyQuestionsState()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(state.questions) { index, question ->
                    QuestionCard(
                        question = question,
                        index = index,
                        isSelected = index == state.currentQuestionIndex,
                        totalQuestions = state.questions.size, // <-- ДОБАВИЛИ ЭТО
                        onSelect = {
                            println("🎯 Клик на вопрос $index")
                            onEvent(SurveyManagementEvent.SelectQuestion(index))
                            println("🎯 Вызываем onEditQuestion($index)")
                            onEditQuestion(index)
                            println("🎯 onEditQuestion вызван")
                        },
                        onMoveUp = {
                            if (index > 0) onEvent(SurveyManagementEvent.MoveQuestionUp(index))
                        },
                        onMoveDown = {
                            if (index < state.questions.size - 1) onEvent(SurveyManagementEvent.MoveQuestionDown(index))
                        },
                        onDelete = { onEvent(SurveyManagementEvent.DeleteQuestion(index)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Показываем ошибки валидации
        if (state.surveyValidationErrors.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Исправьте ошибки:",
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                    state.surveyValidationErrors.forEach { error ->
                        Text(
                            text = "• $error",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Кнопка сохранения
        Button(
            onClick = { onEvent(SurveyManagementEvent.SaveSurvey) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = state.isSurveyValid && !state.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Создать опрос", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun EmptyQuestionsState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2196F3).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text("?", fontSize = 24.sp, color = Color(0xFF2196F3))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Добавьте вопросы к опросу",
                color = Color(0xFF666666),
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Нажмите кнопку '+' чтобы добавить первый вопрос",
                color = Color(0xFF999999),
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun QuestionCard(
    question: QuestionUiModel,
    index: Int,
    isSelected: Boolean,
    totalQuestions: Int, // <-- ДОБАВИЛИ ЭТОТ ПАРАМЕТР
    onSelect: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Заголовок вопроса с номером
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Цветной кружок с номером вместо иконки
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
                            text = "${index + 1}",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Вопрос ${index + 1}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )

                        if (question.text.isNotBlank()) {
                            Text(
                                text = question.text.take(50) + if (question.text.length > 50) "..." else "",
                                fontSize = 12.sp,
                                color = Color(0xFF666666),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        } else {
                            Text(
                                text = "Текст не заполнен",
                                fontSize = 12.sp,
                                color = Color(0xFF999999),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }

                // Бейдж типа вопроса
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (question.type) {
                                "text" -> Color(0xFFE8F5E9)
                                "voice" -> Color(0xFFE3F2FD)
                                "picture" -> Color(0xFFF3E5F5)
                                "combined" -> Color(0xFFFFF3E0)
                                else -> Color(0xFFF5F5F5)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (question.type) {
                            "text" -> "Текст"
                            "voice" -> "Голос"
                            "picture" -> "Изображение"
                            "combined" -> "Комбинированный"
                            else -> "?"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (question.type) {
                            "text" -> Color(0xFF2E7D32)
                            "voice" -> Color(0xFF1565C0)
                            "picture" -> Color(0xFF7B1FA2)
                            "combined" -> Color(0xFFEF6C00)
                            else -> Color(0xFF666666)
                        }
                    )
                }
            }

            // Индикаторы содержимого вопроса
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    if (question.voiceFilename != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE3F2FD))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Голос", fontSize = 10.sp, color = Color(0xFF1565C0))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (question.pictureFilename != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFF3E5F5))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Изображение", fontSize = 10.sp, color = Color(0xFF7B1FA2))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (question.answerOptions.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFFF3E0))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("${question.answerOptions.size} вар.", fontSize = 10.sp, color = Color(0xFFEF6C00))
                        }
                    }
                }

                // Кнопки управления (только если это не единственный вопрос)
                Row {
                    if (index > 0) {
                        TextButton(
                            onClick = onMoveUp,
                            modifier = Modifier.size(32.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF666666)
                            )
                        ) {
                            Text("↑", fontSize = 12.sp)
                        }
                    }

                    if (index < totalQuestions - 1) { // <-- ИСПРАВЛЕНО: используем totalQuestions
                        TextButton(
                            onClick = onMoveDown,
                            modifier = Modifier.size(32.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF666666)
                            )
                        ) {
                            Text("↓", fontSize = 12.sp)
                        }
                    }

                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Text("✕", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}