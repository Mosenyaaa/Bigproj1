// presentation/Screen/SelectQuestionScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.bigproj.data.api.QuestionResponseDto
import com.example.bigproj.domain.repository.SurveyManagementRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectQuestionScreen(
    surveyId: Int,
    onBack: () -> Unit,
    onQuestionAdded: () -> Unit,
    navController: NavHostController? = null
) {
    val context = LocalContext.current
    val repository = remember { SurveyManagementRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isLoading by remember { mutableStateOf(true) }
    var questions by remember { mutableStateOf<List<QuestionResponseDto>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Все типы") }

    val questionTypes = listOf(
        "Все типы",
        "Текстовый",
        "Один выбор",
        "Несколько вариантов",
        "Шкала",
        "Голос",
        "Фото"
    )

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            questions = repository.getAvailableQuestions(limit = 100)
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
            println("❌ Ошибка загрузки вопросов: ${e.message}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Выбрать вопрос",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    ElevatedButton(
                        onClick = {
                            navController?.navigate("create_question")
                        },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = Color(0xFF006FFD),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Создать вопрос",
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Создать вопрос", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8FAFF)),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Поисковая строка
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Поиск",
                        tint = Color(0xFF666666),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Поиск вопросов...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }

            // Фильтр по типам
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                var showFilter by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFilter = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Фильтр",
                                tint = Color(0xFF666666),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = selectedFilter,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1A1A1A)
                            )
                        }
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Развернуть",
                            tint = Color(0xFF666666),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showFilter,
                        onDismissRequest = { showFilter = false },
                        modifier = Modifier
                            .width(250.dp)
                            .background(Color.White)
                    ) {
                        questionTypes.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = type,
                                        fontSize = 16.sp,
                                        color = if (type == selectedFilter) Color(0xFF006FFD) else Color(0xFF1A1A1A),
                                        fontWeight = if (type == selectedFilter) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    selectedFilter = type
                                    showFilter = false
                                },
                                leadingIcon = {
                                    if (type == selectedFilter) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Выбрано",
                                            tint = Color(0xFF006FFD)
                                        )
                                    }
                                },
                                modifier = Modifier.background(
                                    if (type == selectedFilter) Color(0xFFE8F4FF) else Color.Transparent
                                )
                            )
                        }
                    }
                }
            }

            // Список вопросов
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF006FFD),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Загрузка вопросов...",
                            fontSize = 16.sp,
                            color = Color(0xFF666666)
                        )
                    }
                }
            } else {
                // Фильтрация вопросов
                val filteredQuestions = questions.filter { question ->
                    val matchesSearch = searchQuery.isBlank() ||
                            question.text?.lowercase()?.contains(searchQuery.lowercase()) == true

                    val questionDisplayType = getQuestionDisplayType(question)
                    val matchesType = when (selectedFilter) {
                        "Все типы" -> true
                        "Текстовый" -> questionDisplayType == QuestionDisplayType.TEXT
                        "Один выбор" -> questionDisplayType == QuestionDisplayType.SINGLE_CHOICE
                        "Несколько вариантов" -> questionDisplayType == QuestionDisplayType.MULTIPLE_CHOICE
                        "Шкала" -> questionDisplayType == QuestionDisplayType.SCALE
                        "Голос" -> questionDisplayType == QuestionDisplayType.VOICE
                        "Фото" -> questionDisplayType == QuestionDisplayType.PHOTO
                        else -> true
                    }

                    matchesSearch && matchesType
                }

                if (filteredQuestions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QuestionAnswer,
                                contentDescription = "Нет вопросов",
                                modifier = Modifier.size(64.dp),
                                tint = Color(0xFFCCCCCC)
                            )
                            Text(
                                text = if (searchQuery.isNotBlank())
                                    "Вопросы не найдены"
                                else
                                    "Нет доступных вопросов",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1A1A1A)
                            )
                            Text(
                                text = if (searchQuery.isNotBlank())
                                    "Попробуйте изменить запрос или выбрать другой фильтр"
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
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredQuestions) { question ->
                            EnhancedQuestionItem(
                                question = question,
                                onClick = {
                                    coroutineScope.launch {
                                        try {
                                            println("➕ Добавляем вопрос ${question.id} в опрос $surveyId")

                                            val response = repository.addQuestionToSurvey(
                                                surveyId = surveyId,
                                                questionId = question.id,
                                                orderIndex = 0
                                            )

                                            println("✅ Вопрос добавлен успешно")

                                            snackbarHostState.showSnackbar(
                                                message = "Вопрос добавлен к опросу",
                                                duration = SnackbarDuration.Short
                                            )

                                            onQuestionAdded()

                                        } catch (e: Exception) {
                                            println("❌ Ошибка добавления вопроса: ${e.message}")

                                            snackbarHostState.showSnackbar(
                                                message = "Ошибка: ${e.message}",
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedQuestionItem(
    question: QuestionResponseDto,
    onClick: () -> Unit
) {
    val displayType = getQuestionDisplayType(question)
    val (typeIcon, typeLabel) = when (displayType) {
        QuestionDisplayType.TEXT -> Pair(Icons.Default.TextFields, "Текстовый")
        QuestionDisplayType.SINGLE_CHOICE -> Pair(Icons.Default.RadioButtonChecked, "Один выбор")
        QuestionDisplayType.MULTIPLE_CHOICE -> Pair(Icons.Default.CheckBox, "Несколько вариантов")
        QuestionDisplayType.SCALE -> Pair(Icons.Default.LinearScale, "Шкала")
        QuestionDisplayType.VOICE -> Pair(Icons.Default.Mic, "Голос")
        QuestionDisplayType.PHOTO -> Pair(Icons.Default.Photo, "Фото")
        else -> Pair(Icons.Default.TextFields, "Текстовый")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Иконка типа
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F4FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = typeIcon,
                    contentDescription = typeLabel,
                    tint = Color(0xFF006FFD),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Информация
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = question.text ?: "Без текста",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF1A1A1A)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Тип вопроса
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF006FFD))
                        )
                        Text(
                            text = typeLabel,
                            fontSize = 14.sp,
                            color = Color(0xFF666666)
                        )
                    }

                    // Бейдж публичности
                    if (question.isPublic == true) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE3F2FD))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Публичный",
                                fontSize = 12.sp,
                                color = Color(0xFF1565C0),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Информация о вариантах
                question.answerOptions?.let { options ->
                    if (options.isNotEmpty()) {
                        Text(
                            text = "${options.size} вариант${getPluralEnding(options.size)}",
                            fontSize = 13.sp,
                            color = Color(0xFF888888)
                        )
                    }
                }
            }

            // Стрелочка выбора
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "Выбрать",
                tint = Color(0xFFCCCCCC),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Вспомогательные функции
enum class QuestionDisplayType {
    TEXT,
    SINGLE_CHOICE,
    MULTIPLE_CHOICE,
    SCALE,
    VOICE,
    PHOTO,
    COMBINED
}

fun getQuestionDisplayType(question: QuestionResponseDto): QuestionDisplayType {
    return when {
        question.voiceFilename != null && question.pictureFilename != null -> QuestionDisplayType.COMBINED
        question.voiceFilename != null -> QuestionDisplayType.VOICE
        question.pictureFilename != null -> QuestionDisplayType.PHOTO
        !question.answerOptions.isNullOrEmpty() -> {
            val isNumeric = question.answerOptions.all { it.toIntOrNull() != null }
            val isMultiple = question.extraData?.get("multiple_choice") == "true"
            when {
                isNumeric -> QuestionDisplayType.SCALE
                isMultiple -> QuestionDisplayType.MULTIPLE_CHOICE
                else -> QuestionDisplayType.SINGLE_CHOICE
            }
        }
        else -> QuestionDisplayType.TEXT
    }
}

fun getPluralEnding(count: Int): String {
    return when {
        count % 10 == 1 && count % 100 != 11 -> ""
        count % 10 in 2..4 && count % 100 !in 12..14 -> "а"
        else -> "ов"
    }
}