// presentation/Screen/EditSurveyScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.example.bigproj.data.api.QuestionResponseDto
import com.example.bigproj.data.model.QuestionTypes
import com.example.bigproj.domain.repository.SurveyManagementRepository
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Редактировать опрос",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!state.isLoading) {
                            Text(
                                text = "${state.questions.size} вопросов",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
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
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Survey title and question count - УБРАЛИ ПОВТОРЯЮЩУЮСЯ ИНФОРМАЦИЮ

                // Survey info card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Title field - СДЕЛАЛИ КРУПНЕЕ
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Название опроса",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF444444),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = state.title,
                                onValueChange = { viewModel.onEvent(EditSurveyEvent.UpdateTitle(it)) },
                                label = null,
                                placeholder = { Text("Введите название опроса") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF006FFD),
                                    unfocusedBorderColor = Color(0xFFCCCCCC)
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Description field
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Описание опроса",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF444444),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = state.description,
                                onValueChange = { viewModel.onEvent(EditSurveyEvent.UpdateDescription(it)) },
                                label = null,
                                placeholder = { Text("Введите описание опроса") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF006FFD),
                                    unfocusedBorderColor = Color(0xFFCCCCCC)
                                ),
                                minLines = 3,
                                maxLines = 5
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Status selector
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Статус опроса",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF444444),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            ExposedDropdownMenuBox(
                                expanded = showStatusDropdown,
                                onExpandedChange = { showStatusDropdown = !showStatusDropdown }
                            ) {
                                OutlinedTextField(
                                    value = formatStatus(state.status),
                                    onValueChange = {},
                                    readOnly = true,
                                    label = null,
                                    placeholder = { Text("Выберите статус") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStatusDropdown)
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF006FFD),
                                        unfocusedBorderColor = Color(0xFFCCCCCC)
                                    )
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
                }

                // Questions section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Вопросы",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A1A)
                            )

                            Button(
                                onClick = {
                                    // Навигация на экран выбора вопроса вместо диалога
                                    navController?.navigate("select_question/${surveyId}")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF006FFD)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Добавить",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text("Добавить вопрос", fontSize = 14.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Questions list
                        if (state.questions.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.QuestionAnswer,
                                        contentDescription = "Нет вопросов",
                                        modifier = Modifier.size(48.dp),
                                        tint = Color(0xFFCCCCCC)
                                    )
                                    Text(
                                        text = "Нет вопросов",
                                        fontSize = 16.sp,
                                        color = Color(0xFF666666)
                                    )
                                    Text(
                                        text = "Добавьте вопросы к опросу",
                                        fontSize = 14.sp,
                                        color = Color(0xFF999999)
                                    )
                                }
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                state.questions.forEachIndexed { index, question ->
                                    ImprovedSurveyQuestionCard(
                                        question = question,
                                        index = index,
                                        totalQuestionsCount = state.questions.size,
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
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Save button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Button(
                        onClick = { viewModel.onEvent(EditSurveyEvent.SaveSurvey) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF006FFD)
                        )
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Сохранить изменения", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }

    // Delete question confirmation
    showDeleteQuestionDialog?.let { question ->
        AlertDialog(
            onDismissRequest = { showDeleteQuestionDialog = null },
            title = { Text("Удалить вопрос?") },
            text = {
                Column {
                    Text("Вы уверены, что хотите удалить вопрос:")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${question.questionText?.take(50) ?: "Вопрос"}...\"",
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Это действие нельзя отменить.",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
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
fun ImprovedSurveyQuestionCard(
    question: QuestionInSurveyDto,
    index: Int,
    totalQuestionsCount: Int,
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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Номер и информация
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Номер вопроса
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F4FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${index + 1}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF006FFD)
                        )
                    }

                    // Тип вопроса с цветным бейджем
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(getQuestionTypeColorForCard(question))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = getQuestionTypeIconForCard(question),
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }

                    Text(
                        text = getQuestionTypeLabelForCard(question),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666)
                    )
                }

                // Действия
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Кнопки перемещения
                    IconButton(
                        onClick = onMoveUp,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFFF5F5F5)
                        ),
                        enabled = index > 0
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Вверх",
                            modifier = Modifier.size(16.dp),
                            tint = if (index > 0) Color(0xFF666666) else Color(0xFFCCCCCC)
                        )
                    }

                    IconButton(
                        onClick = onMoveDown,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFFF5F5F5)
                        ),
                        enabled = index < totalQuestionsCount - 1
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Вниз",
                            modifier = Modifier.size(16.dp),
                            tint = if (index < totalQuestionsCount - 1) Color(0xFF666666) else Color(0xFFCCCCCC)
                        )
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Редактировать",
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFF006FFD)
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить",
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFFC62828)
                        )
                    }
                }
            }

            // Текст вопроса
            Text(
                text = question.questionText ?: "Без текста",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(top = 12.dp, start = 44.dp)
            )

            // Информация о вариантах ответов
            question.answerOptions?.let { options ->
                if (options.isNotEmpty()) {
                    Text(
                        text = "📋 ${options.size} ${getAnswerOptionsCountLabel(options.size)}",
                        fontSize = 13.sp,
                        color = Color(0xFF888888),
                        modifier = Modifier.padding(top = 8.dp, start = 44.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImprovedAddQuestionToSurveyDialog(
    onDismiss: () -> Unit,
    onQuestionSelected: (Int) -> Unit,
    navController: NavHostController? = null
) {
    val context = LocalContext.current
    val repository = remember { SurveyManagementRepository(context) }

    var isLoading by remember { mutableStateOf(true) }
    var questions by remember { mutableStateOf<List<QuestionResponseDto>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Все типы") }

    val questionTypes = listOf(
        "Все типы",
        "Текстовый ответ",
        "Один выбор",
        "Несколько вариантов",
        "Шкала (1-10)",
        "Голосовой ответ",
        "Фото"
    )

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            questions = repository.getAvailableQuestions(query = null, start = 0, finish = null, limit = 100)
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            println("❌ Ошибка загрузки вопросов: ${e.message}")
        }
    }

    // Фильтруем вопросы по поиску и типу
    val filteredQuestions = questions.filter { question ->
        val matchesSearch = searchQuery.isBlank() ||
                question.text?.lowercase()?.contains(searchQuery.lowercase()) == true

        val matchesType = when (selectedFilter) {
            "Все типы" -> true
            "Текстовый ответ" -> question.answerOptions.isNullOrEmpty() &&
                    question.voiceFilename == null &&
                    question.pictureFilename == null
            "Один выбор" -> !question.answerOptions.isNullOrEmpty() &&
                    (question.extraData?.get("multiple_choice") != "true")
            "Несколько вариантов" -> !question.answerOptions.isNullOrEmpty() &&
                    (question.extraData?.get("multiple_choice") == "true")
            "Шкала (1-10)" -> {
                val options = question.answerOptions
                !options.isNullOrEmpty() && options.all { it.toIntOrNull() != null }
            }
            "Голосовой ответ" -> question.voiceFilename != null
            "Фото" -> question.pictureFilename != null
            else -> true
        }

        matchesSearch && matchesType
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Заголовок с кнопкой закрытия
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Добавить вопрос",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5F5F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✕", fontSize = 18.sp, color = Color(0xFF666666))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Поиск
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Поиск вопросов...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Поиск",
                            tint = Color(0xFF666666)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8F9FA),
                        unfocusedContainerColor = Color(0xFFF8F9FA),
                        focusedBorderColor = Color(0xFF006FFD),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Фильтр по типам
                var showFilterDropdown by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFilterDropdown = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF8F9FA)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedFilter,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1A1A1A)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Фильтр",
                                tint = Color(0xFF666666)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showFilterDropdown,
                        onDismissRequest = { showFilterDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .background(Color.White)
                    ) {
                        questionTypes.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = type,
                                        fontSize = 16.sp,
                                        color = if (type == selectedFilter) Color(0xFF006FFD) else Color(0xFF1A1A1A)
                                    )
                                },
                                onClick = {
                                    selectedFilter = type
                                    showFilterDropdown = false
                                },
                                modifier = Modifier.background(
                                    if (type == selectedFilter) Color(0xFFE8F4FF) else Color.Transparent
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Список вопросов
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF006FFD),
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "Загрузка вопросов...",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                } else if (filteredQuestions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🎯",
                                fontSize = 40.sp
                            )
                            Text(
                                text = if (searchQuery.isNotBlank()) "Вопросы не найдены" else "Нет доступных вопросов",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1A1A1A)
                            )
                            Text(
                                text = if (searchQuery.isNotBlank())
                                    "Попробуйте другой запрос или выберите другой фильтр"
                                else
                                    "Создайте новый вопрос",
                                fontSize = 14.sp,
                                color = Color(0xFF666666),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredQuestions) { question ->
                            ImprovedQuestionSelectionCard(
                                question = question,
                                onClick = {
                                    onQuestionSelected(question.id)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Кнопки действий
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Text(
                            text = "Закрыть",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF666666)
                        )
                    }
                    Button(
                        onClick = {
                            onDismiss()
                            navController?.navigate("create_question")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF006FFD),
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Создать вопрос", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImprovedQuestionSelectionCard(
    question: QuestionResponseDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Текст вопроса
            Text(
                text = question.text ?: "Без текста",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A1A1A),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Информация о типе и публичности
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Иконка типа
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(getQuestionTypeColor(question))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = getQuestionTypeIcon(question),
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    // Название типа
                    Text(
                        text = getQuestionTypeDisplayName(question),
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }

                // Бейдж публичности
                if (question.isPublic == true) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                        )
                        Text(
                            text = "Публичный",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            // Дополнительная информация
            question.answerOptions?.let { options ->
                if (options.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${options.size} ${getAnswerOptionsCountLabel(options.size)}",
                        fontSize = 13.sp,
                        color = Color(0xFF888888)
                    )
                }
            }
        }
    }
}

// Вспомогательные функции для определения типа вопроса
private fun getQuestionTypeColor(question: QuestionResponseDto): Color {
    return when {
        question.voiceFilename != null && question.pictureFilename != null -> Color(0xFF9C27B0) // Фиолетовый
        question.voiceFilename != null -> Color(0xFF2196F3) // Синий
        question.pictureFilename != null -> Color(0xFF4CAF50) // Зеленый
        !question.answerOptions.isNullOrEmpty() -> {
            val isNumeric = question.answerOptions.all { it.toIntOrNull() != null }
            if (isNumeric) Color(0xFFFF9800) // Оранжевый
            else Color(0xFFE91E63) // Розовый
        }
        else -> Color(0xFF795548) // Коричневый
    }
}

private fun getQuestionTypeIcon(question: QuestionResponseDto): String {
    return when {
        question.voiceFilename != null && question.pictureFilename != null -> "🔗"
        question.voiceFilename != null -> "🎤"
        question.pictureFilename != null -> "🖼️"
        !question.answerOptions.isNullOrEmpty() -> {
            val isNumeric = question.answerOptions.all { it.toIntOrNull() != null }
            if (isNumeric) "📊" else "☑️"
        }
        else -> "📝"
    }
}

private fun getQuestionTypeDisplayName(question: QuestionResponseDto): String {
    return when {
        question.voiceFilename != null && question.pictureFilename != null -> "Комбинированный"
        question.voiceFilename != null -> "Голосовой ответ"
        question.pictureFilename != null -> "Фото"
        !question.answerOptions.isNullOrEmpty() -> {
            val isNumeric = question.answerOptions.all { it.toIntOrNull() != null }
            val isMultiple = question.extraData?.get("multiple_choice") == "true"
            when {
                isNumeric -> "Шкала (1-10)"
                isMultiple -> "Несколько вариантов"
                else -> "Один выбор"
            }
        }
        else -> "Текстовый ответ"
    }
}

// Вспомогательные функции для карточек
private fun getQuestionTypeColorForCard(question: QuestionInSurveyDto): Color {
    return when {
        question.voiceFilename != null && question.pictureFilename != null -> Color(0xFF9C27B0)
        question.voiceFilename != null -> Color(0xFF2196F3)
        question.pictureFilename != null -> Color(0xFF4CAF50)
        question.answerOptions != null && question.answerOptions.isNotEmpty() -> {
            val isNumeric = question.answerOptions.all { it.toIntOrNull() != null }
            if (isNumeric) Color(0xFFFF9800) else Color(0xFFE91E63)
        }
        else -> Color(0xFF795548)
    }
}

private fun getQuestionTypeIconForCard(question: QuestionInSurveyDto): String {
    return when {
        question.voiceFilename != null && question.pictureFilename != null -> "🔗"
        question.voiceFilename != null -> "🎤"
        question.pictureFilename != null -> "🖼️"
        question.answerOptions != null && question.answerOptions.isNotEmpty() -> {
            val isNumeric = question.answerOptions.all { it.toIntOrNull() != null }
            if (isNumeric) "📊" else "☑️"
        }
        else -> "📝"
    }
}

private fun getQuestionTypeLabelForCard(question: QuestionInSurveyDto): String {
    return when {
        question.voiceFilename != null && question.pictureFilename != null -> "Комбинированный"
        question.voiceFilename != null -> "Голосовой"
        question.pictureFilename != null -> "Фото"
        question.answerOptions != null && question.answerOptions.isNotEmpty() -> {
            val isNumeric = question.answerOptions.all { it.toIntOrNull() != null }
            val isMultiple = question.extraData?.get("multiple_choice") == "true"
            when {
                isNumeric -> "Шкала"
                isMultiple -> "Несколько вариантов"
                else -> "Один выбор"
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

private fun formatStatus(status: String): String {
    return when (status.lowercase()) {
        "draft" -> "Черновик"
        "active" -> "Активный"
        "archived" -> "Архив"
        else -> status
    }
}