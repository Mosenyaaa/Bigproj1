// presentation/Screen/EditSurveyScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
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
import androidx.navigation.NavHostController
import com.example.bigproj.data.api.QuestionInSurveyDto
import com.example.bigproj.data.model.QuestionTypes
import com.example.bigproj.presentation.Screen.state.EditSurveyEvent
import com.example.bigproj.presentation.Screen.viewmodel.EditSurveyViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSurveyScreen(
    surveyId: Int,
    navController: NavHostController? = null
) {
    val context = LocalContext.current
    val viewModel = viewModel<EditSurveyViewModel>()
    val state = viewModel.state

    val snackbarHostState = remember { SnackbarHostState() }
    var showStatusDropdown by remember { mutableStateOf(false) }
    var showDeleteQuestionDialog by remember { mutableStateOf<QuestionInSurveyDto?>(null) }

    LaunchedEffect(surveyId) {
        viewModel.setupDependencies(context)
        viewModel.setSurveyId(surveyId)
        viewModel.onEvent(EditSurveyEvent.LoadSurvey)
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

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            snackbarHostState.showSnackbar(
                message = "Изменения сохранены",
                duration = SnackbarDuration.Short
            )
            delay(2000)
            viewModel.resetSuccess()
        }
    }

    LaunchedEffect(state.questions) {

    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Survey") },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (state.isLoading && state.survey == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Survey title and question count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.title,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A)
                        )
                        Text(
                            text = "${state.questions.size} вопросов",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Survey info card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Title field
                        OutlinedTextField(
                            value = state.title,
                            onValueChange = { viewModel.onEvent(EditSurveyEvent.UpdateTitle(it)) },
                            label = { Text("Название опроса") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Description field
                        OutlinedTextField(
                            value = state.description,
                            onValueChange = { viewModel.onEvent(EditSurveyEvent.UpdateDescription(it)) },
                            label = { Text("Описание опроса") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            shape = RoundedCornerShape(8.dp),
                            minLines = 3,
                            maxLines = 5
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status selector
                        ExposedDropdownMenuBox(
                            expanded = showStatusDropdown,
                            onExpandedChange = { showStatusDropdown = !showStatusDropdown }
                        ) {
                            OutlinedTextField(
                                value = formatStatus(state.status),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Статус") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStatusDropdown)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = showStatusDropdown,
                                onDismissRequest = { showStatusDropdown = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Черновик") },
                                    onClick = {
                                        viewModel.changeSurveyStatus("draft")
                                        showStatusDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Активный") },
                                    onClick = {
                                        viewModel.changeSurveyStatus("active")
                                        showStatusDropdown = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Архив") },
                                    onClick = {
                                        viewModel.changeSurveyStatus("archived")
                                        showStatusDropdown = false
                                    }
                                )
                            }
                        }

                    }
                }

                // Questions section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Questions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    
                    Button(
                        onClick = { viewModel.onEvent(EditSurveyEvent.ShowAddQuestionDialog) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("+")
                            Text("Add")
                        }
                    }
                }

                // Questions list
                if (state.questions.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Нет вопросов",
                                fontSize = 16.sp,
                                color = Color(0xFF666666)
                            )
                            Text(
                                text = "Добавьте вопросы к опросу",
                                fontSize = 14.sp,
                                color = Color(0xFF999999),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                } else {
                    state.questions.forEachIndexed { index, question ->
                        SurveyQuestionCard(
                            question = question,
                            index = index,
                            onEditClick = {
                                navController?.navigate("edit_question/${question.questionId}")
                            },
                            onDeleteClick = {
                                showDeleteQuestionDialog = question
                            },
                            onMoveUp = {
                                if (index > 0) {
                                    viewModel.onEvent(EditSurveyEvent.SwapQuestions(index - 1, index))
                                }
                            },
                            onMoveDown = {
                                if (index < state.questions.size - 1) {
                                    viewModel.onEvent(EditSurveyEvent.SwapQuestions(index, index + 1))
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Save button
                Button(
                    onClick = { viewModel.onEvent(EditSurveyEvent.SaveSurvey) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Save Changes")
                    }
                }
            }
        }
    }

    // Add question dialog
    if (state.showAddQuestionDialog) {
        AddQuestionToSurveyDialog(
            onDismiss = { viewModel.onEvent(EditSurveyEvent.HideAddQuestionDialog) },
            onQuestionSelected = { questionId ->
                viewModel.onEvent(EditSurveyEvent.AddQuestionToSurvey(questionId))
            },
            navController = navController
        )
    }

    // Delete question confirmation
    showDeleteQuestionDialog?.let { question ->
        AlertDialog(
            onDismissRequest = { showDeleteQuestionDialog = null },
            title = { Text("Удалить вопрос?") },
            text = {
                Text("Вы уверены, что хотите удалить вопрос \"${question.questionText?.take(50) ?: "Вопрос"}...\" из опроса?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onEvent(EditSurveyEvent.RemoveQuestion(question.questionInSurveyId))
                        showDeleteQuestionDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC62828)
                    )
                ) {
                    Text("Удалить", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteQuestionDialog = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun SurveyQuestionCard(
    question: QuestionInSurveyDto,
    index: Int,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Question number and info
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Question number badge
                Text(
                    text = "#${index + 1}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    // Question type icon and label
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = getQuestionTypeIcon(question),
                            fontSize = 16.sp
                        )
                        Text(
                            text = getQuestionTypeLabel(question),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1A1A1A)
                        )
                    }

                    // Question text
                    Text(
                        text = question.questionText ?: "Без текста",
                        fontSize = 14.sp,
                        color = Color(0xFF1A1A1A),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    // Answer options count (if any)
                    if (question.answerOptions != null && question.answerOptions.isNotEmpty()) {
                        Text(
                            text = "${question.answerOptions.size} ${getAnswerOptionsCountLabel(question.answerOptions.size)}",
                            fontSize = 12.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    
                }
            }

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Text("✏️", fontSize = 18.sp)
                }
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Text("🗑️", fontSize = 18.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuestionToSurveyDialog(
    onDismiss: () -> Unit,
    onQuestionSelected: (Int) -> Unit,
    navController: NavHostController? = null
) {
    val context = LocalContext.current
    val repository = remember { com.example.bigproj.domain.repository.SurveyManagementRepository(context) }
    
    var isLoading by remember { mutableStateOf(true) }
    var questions by remember { mutableStateOf<List<com.example.bigproj.data.api.QuestionResponseDto>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            questions = repository.getAvailableQuestions(query = null, start = 0, finish = null, limit = 50)
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            println("❌ Ошибка загрузки вопросов: ${e.message}")
        }
    }
    
    val filteredQuestions = if (searchQuery.isBlank()) {
        questions
    } else {
        val lowerQuery = searchQuery.lowercase()
        questions.filter { question ->
            question.text?.lowercase()?.contains(lowerQuery) == true
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Question",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Text("✕", fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search questions...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Text("🔍", fontSize = 18.sp)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Questions list
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (filteredQuestions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "Нет доступных вопросов" else "Вопросы не найдены",
                            fontSize = 16.sp,
                            color = Color(0xFF666666)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredQuestions) { question ->
                            QuestionSelectionCard(
                                question = question,
                                onClick = {
                                    onQuestionSelected(question.id)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }
                    Button(
                        onClick = {
                            onDismiss()
                            navController?.navigate("create_question")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("+")
                            Text("Create Question")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionSelectionCard(
    question: com.example.bigproj.data.api.QuestionResponseDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Type icon
            Text(
                text = getQuestionTypeIconForDialog(question),
                fontSize = 24.sp
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = question.text ?: "Без текста",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (question.answerOptions != null && question.answerOptions.isNotEmpty()) {
                    Text(
                        text = "${question.answerOptions.size} вариантов",
                        fontSize = 12.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // Public badge
            if (question.isPublic == true) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE3F2FD))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Public", fontSize = 10.sp, color = Color(0xFF1565C0))
                }
            }
        }
    }
}

private fun getQuestionTypeIconForDialog(question: com.example.bigproj.data.api.QuestionResponseDto): String {
    return when {
        question.voiceFilename != null && question.pictureFilename != null -> "🔗"
        question.voiceFilename != null -> "🎤"
        question.pictureFilename != null -> "🖼️"
        question.answerOptions != null && question.answerOptions.isNotEmpty() -> "☑️"
        else -> "📝"
    }
}

private fun formatStatus(status: String): String {
    return when (status.lowercase()) {
        "draft" -> "Черновик"
        "active" -> "Активный"
        "archived" -> "Архив"
        else -> status
    }
}

private fun getQuestionTypeIcon(question: QuestionInSurveyDto): String {
    return when {
        question.voiceFilename != null && question.pictureFilename != null -> "🔗"
        question.voiceFilename != null -> "🔊"
        question.pictureFilename != null -> "🖼️"
        question.answerOptions != null && question.answerOptions.isNotEmpty() -> {
            // Check if it's a scale (numeric options) or choice
            val isNumeric = question.answerOptions.all { it.toIntOrNull() != null }
            if (isNumeric) "📊" else "☑️"
        }
        else -> "📝"
    }
}

private fun getQuestionTypeLabel(question: QuestionInSurveyDto): String {
    return when {
        question.voiceFilename != null && question.pictureFilename != null -> "Комбинированный"
        question.voiceFilename != null -> "Голосовой"
        question.pictureFilename != null -> "Фото"
        question.answerOptions != null && question.answerOptions.isNotEmpty() -> {
            // Check if it's a scale (numeric options)
            val isNumeric = question.answerOptions.all { it.toIntOrNull() != null }
            if (isNumeric) {
                "Шкала"
            } else {
                // Check if multiple choice
                val isMultiple = question.extraData?.get("multiple_choice") == "true"
                if (isMultiple) "Множественный выбор" else "Один выбор"
            }
        }
        else -> "Текстовый"
    }
}

private fun getAnswerOptionsCountLabel(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> "вариант"
        count % 10 in 2..4 && count % 100 !in 12..14 -> "варианта"
        else -> "вариантов"
    }
}
