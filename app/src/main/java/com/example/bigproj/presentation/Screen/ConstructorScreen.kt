// presentation/Screen/ConstructorScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.bigproj.data.model.QuestionTypes
import com.example.bigproj.presentation.Screen.state.ConstructorEvent
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
                    Column {
                        Text(
                            text = "Конструктор вопросов",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!state.isLoading) {
                            Text(
                                text = "${state.questionsCount} вопросов",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
            )
        },
        // УБИРАЕМ FAB КНОПКУ ОТСЮДА - она теперь есть в кнопке "Создать вопрос"
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { query ->
                    viewModel.onEvent(ConstructorEvent.SearchQuestions(query))
                },
                placeholder = { Text("Поиск вопросов...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Button to create question (ОСТАВЛЯЕМ ТОЛЬКО ЭТУ КНОПКУ)
            Button(
                onClick = {
                    navController?.navigate("create_question")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("+ Создать вопрос", fontSize = 16.sp)
            }

            // Questions list
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.filteredQuestions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.searchQuery.isNotBlank()) "Вопросы не найдены" else "Нет вопросов",
                        fontSize = 16.sp,
                        color = Color(0xFF666666)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // Добавляем отступ снизу для BottomNavigation
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
                        Text("Вы уверены, что хотите удалить вопрос \"${questionToDelete.text?.take(50) ?: "Без текста"}\"?")
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
    // Determine display type and icon based on question data
    // Check combined first, then individual media types, then options, then text
    val (typeIcon, typeLabel) = when {
        question.pictureFilename != null && question.voiceFilename != null -> "🔗" to "Комбинированный"
        question.pictureFilename != null -> "🖼️" to "Фото"
        question.voiceFilename != null -> "🔊" to "Голосовой"
        question.answerOptions != null && question.answerOptions.isNotEmpty() -> {
            // Check if it's a scale (numeric options)
            val isNumeric = question.answerOptions.all { it.toIntOrNull() != null }
            if (isNumeric) {
                "📊" to "Шкала"
            } else {
                // Check if multiple choice in extra_data
                val isMultiple = question.extraData?.get("multiple_choice") == "true"
                if (isMultiple) {
                    "☑️☑️" to "Множественный"
                } else {
                    "☑️" to "Один выбор"
                }
            }
        }
        else -> "📝" to "Текстовый"
    }
    
    // Get description - try to extract from text if it contains separator, or from extra_data
    val description = if (question.text != null && question.text.contains("\n\n")) {
        question.text.split("\n\n", limit = 2).getOrNull(1) ?: question.extraData?.get("description")
    } else {
        question.extraData?.get("description")
    }
    
    // Determine if required (default to false, can be in extra_data)
    val isRequired = question.extraData?.get("is_required")?.toBoolean() ?: false
    
    // Get answer options count
    val optionsCount = question.answerOptions?.size ?: 0
    
    // Get question text without description
    val questionText = if (question.text != null && question.text.contains("\n\n")) {
        question.text.split("\n\n", limit = 2)[0]
    } else {
        question.text
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Main content (clickable)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick)
            ) {
                // Question text
                Text(
                    text = questionText ?: "Без текста",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                // Description
                description?.let {
                    if (it.isNotBlank()) {
                        Text(
                            text = it,
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Question metadata row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type icon and label
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = typeIcon, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = typeLabel,
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                    }

                    // Required badge
                    if (isRequired) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Обязательный",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFC62828)
                            )
                        }
                    }

                    // Answer options count
                    if (optionsCount > 0) {
                        Text(
                            text = "$optionsCount ${getOptionsWord(optionsCount)}",
                            fontSize = 12.sp,
                            color = Color(0xFF888888)
                        )
                    }
                }
            }

            // Action buttons (edit and delete)
            Row(
                modifier = Modifier.padding(start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Text("✏️", fontSize = 18.sp)
                }
                onDelete?.let {
                    IconButton(
                        onClick = it,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Text("🗑️", fontSize = 18.sp)
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
