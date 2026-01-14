// presentation/Screen/ConstructorScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bigproj.data.api.QuestionResponseDto
import com.example.bigproj.presentation.Screen.state.ConstructorEvent
import com.example.bigproj.presentation.Screen.state.QuestionDisplayType
import com.example.bigproj.presentation.Screen.viewmodel.ConstructorViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstructorScreen(
    navController: NavHostController? = null
) {
    val context = LocalContext.current
    val viewModel = viewModel<ConstructorViewModel>()
    val state = viewModel.state

    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf<QuestionResponseDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.setupDependencies(context)
        viewModel.onEvent(ConstructorEvent.LoadQuestions)
    }

    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = state.errorMessage,
                duration = SnackbarDuration.Short
            )
            delay(3000)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Конструктор вопросов",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!state.isLoading) {
                            Text(
                                text = "${state.questionsCount} вопросов",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            // Search and create button container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Search field
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { query ->
                        viewModel.onEvent(ConstructorEvent.SearchQuestions(query))
                    },
                    placeholder = { Text("Поиск вопросов...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Поиск",
                            tint = Color(0xFF666666)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF006FFD),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // Create question button
                Button(
                    onClick = {
                        navController?.navigate("create_question")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF006FFD)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Создать вопрос",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Создать вопрос", fontSize = 16.sp)
                    }
                }
            }

            // Questions list
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF006FFD)
                    )
                }
            } else if (state.filteredQuestions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QuestionMark,
                            contentDescription = "Нет вопросов",
                            tint = Color(0xFF999999),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (state.searchQuery.isNotBlank())
                                "Вопросы не найдены"
                            else
                                "Нет вопросов",
                            fontSize = 16.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(state.filteredQuestions) { question ->
                        QuestionCard(
                            question = question,
                            onClick = {
                                navController?.navigate("edit_question/${question.id}")
                            },
                            onDelete = {
                                showDeleteDialog = question
                            }
                        )
                    }
                }
            }
        }

        // Delete confirmation dialog
        showDeleteDialog?.let { questionToDelete ->
            questionToDelete.id?.let { questionId ->
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = null },
                    title = { Text("Удалить вопрос?") },
                    text = {
                        Column {
                            Text("Вы уверены, что хотите удалить вопрос:")
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "\"${questionToDelete.text?.take(50) ?: "Без текста"}\"",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.onEvent(ConstructorEvent.DeleteQuestion(questionId))
                                showDeleteDialog = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC62828)
                            )
                        ) {
                            Text("Удалить", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = null }) {
                            Text("Отмена")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun QuestionCard(
    question: QuestionResponseDto,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    // Определяем display type
    val displayType = determineQuestionDisplayType(question)

    // Определяем иконку и метку
    val (typeIcon, typeLabel) = when (displayType) {
        QuestionDisplayType.TEXT -> Pair(Icons.Default.TextFields, "Текстовый")
        QuestionDisplayType.SINGLE_CHOICE -> Pair(Icons.Default.RadioButtonChecked, "Один выбор")
        QuestionDisplayType.MULTIPLE_CHOICE -> Pair(Icons.Default.CheckBox, "Несколько")
        QuestionDisplayType.SCALE -> Pair(Icons.Default.LinearScale, "Шкала")
        QuestionDisplayType.VOICE -> Pair(Icons.Default.Mic, "Голос")
        QuestionDisplayType.PHOTO -> Pair(Icons.Default.Photo, "Фото")
        else -> Pair(Icons.Default.TextFields, "Текстовый")
    }

    // Удаляем маркеры из текста для отображения
    val displayText = question.text?.let { text ->
        text.replace("\\[MULTIPLE_CHOICE\\]".toRegex(), "")
            .replace("\\[SCALE:\\d+-\\d+\\]".toRegex(), "")
            .trim()
    } ?: "Без текста"

    // Разделяем на вопрос и описание
    val (questionText, description) = if (displayText.contains("\n\n")) {
        val parts = displayText.split("\n\n", limit = 2)
        parts[0] to (if (parts.size > 1) parts[1] else "")
    } else {
        displayText to ""
    }

    // Определяем обязательность
    val isRequired = question.extraData?.get("is_required")?.toBoolean() ?: false

    // Получаем количество вариантов ответов
    val optionsCount = question.answerOptions?.size ?: 0

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Question text - как на скриншоте
            Text(
                text = questionText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Description - как на скриншоте
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Question metadata row - КАК НА СКРИНШОТЕ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: type and options
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Type with icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = typeLabel,
                            tint = Color(0xFF006FFD),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = typeLabel,
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Separator
                    if (optionsCount > 0) {
                        Text(
                            text = "|",
                            fontSize = 14.sp,
                            color = Color(0xFFE0E0E0)
                        )

                        // Answer options count
                        Text(
                            text = "$optionsCount ${getOptionsWord(optionsCount)}",
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }

                // Right side: action buttons and required badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Required badge
                    if (isRequired) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Обязательный",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFC62828)
                            )
                        }
                    }

                    // Action buttons - как на скриншоте
                    // Edit button
                    IconButton(
                        onClick = onClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать",
                            tint = Color(0xFF006FFD),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete button
                    onDelete?.let {
                        IconButton(
                            onClick = it,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить",
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getOptionsWord(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "вариант"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "варианта"
        else -> "вариантов"
    }
}

// Функция для определения типа вопроса (аналогичная той, что в ViewModel)
private fun determineQuestionDisplayType(question: QuestionResponseDto): QuestionDisplayType {
    return when (question.type) {
        "voice" -> QuestionDisplayType.VOICE
        "picture" -> QuestionDisplayType.PHOTO
        "combined" -> QuestionDisplayType.PHOTO
        else -> {
            if (question.answerOptions != null && question.answerOptions.isNotEmpty()) {
                val isPublic = question.isPublic ?: false

                // Проверяем если это шкала (все варианты - последовательные числа)
                val numericOptions = question.answerOptions.mapNotNull { it.toIntOrNull() }
                val isSequential = numericOptions.size > 1 &&
                        numericOptions.sorted() == numericOptions &&
                        numericOptions.zipWithNext().all { (a, b) -> b - a == 1 }

                if (isSequential && numericOptions.size >= 3) {
                    QuestionDisplayType.SCALE
                } else if (isPublic && question.answerOptions.size > 1) {
                    QuestionDisplayType.MULTIPLE_CHOICE
                } else {
                    QuestionDisplayType.SINGLE_CHOICE
                }
            } else {
                QuestionDisplayType.TEXT
            }
        }
    }
}