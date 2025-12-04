// presentation/Screen/SurveyDetailScreen.kt
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bigproj.presentation.Screen.state.SurveyScreenEvent
import com.example.bigproj.presentation.Screen.viewmodel.SurveyDetailViewModel
import kotlinx.coroutines.delay

@Composable
fun SurveyDetailScreen(
    surveyId: Int,
    onNavigateBack: () -> Unit = {},
    onSurveyCompleted: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val viewModel = viewModel<SurveyDetailViewModel>()

    LaunchedEffect(Unit) {
        viewModel.setupDependencies(context)
        viewModel.loadSurvey(surveyId)
    }

    LaunchedEffect(viewModel.isSurveySubmitted) {
        if (viewModel.isSurveySubmitted) {
            delay(2000)
            onNavigateBack()
        }
    }

    Scaffold { paddingValues ->
        StepByStepSurveyContent(
            state = viewModel.state,
            onEvent = viewModel::onEvent,
            onNavigateBack = onNavigateBack,
            viewModel = viewModel,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun StepByStepSurveyContent(
    state: com.example.bigproj.presentation.Screen.state.SurveyScreenState,
    onEvent: (SurveyScreenEvent) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SurveyDetailViewModel,
    modifier: Modifier = Modifier
) {
    val questions = viewModel.getRealQuestions()
    val currentQuestion = viewModel.getCurrentQuestion()
    val progress = viewModel.getProgress()
    val currentAnswer = viewModel.userAnswers[viewModel.getCurrentQuestion()?.questionInSurveyId] ?: ""

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFF))
    ) {
        SurveyHeader(
            title = state.currentSurvey?.title ?: "Опрос",
            progress = progress,
            currentQuestion = viewModel.currentQuestionIndex + 1,
            totalQuestions = questions.size,
            onBackClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        when {
            state.isLoading -> {
                LoadingSurveyContent()
            }
            questions.isNotEmpty() && currentQuestion != null -> {
                QuestionContent(
                    question = currentQuestion,
                    userAnswers = viewModel.userAnswers,
                    onAnswerSelected = { answer ->
                        onEvent(SurveyScreenEvent.AnswerQuestion(currentQuestion.questionInSurveyId, answer))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                )

                NavigationButtons(
                    isFirstQuestion = viewModel.isFirstQuestion(),
                    isLastQuestion = viewModel.isLastQuestion(),
                    onPrevious = { onEvent(SurveyScreenEvent.NavigateToPreviousStep) },
                    onNext = { onEvent(SurveyScreenEvent.NavigateToNextStep) },
                    onSubmit = { viewModel.submitSurvey() },
                    currentAnswer = currentAnswer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
            else -> {
                NoQuestionsMessage(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun SurveyHeader(
    title: String,
    progress: Float,
    currentQuestion: Int,
    totalQuestions: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_menu_revert),
                contentDescription = "Назад",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBackClick() }
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = Color(0xFF006FFD),
            trackColor = Color(0xFFE0E0E0)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Вопрос $currentQuestion из $totalQuestions",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
fun LoadingSurveyContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Color(0xFF006FFD)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Загружаем опрос...",
            fontSize = 16.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun QuestionContent(
    question: com.example.bigproj.data.model.QuestionDto,
    userAnswers: Map<Int, String>,
    onAnswerSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentAnswer = userAnswers[question.questionInSurveyId] ?: ""
    var showTextInputDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = question.questionText,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        when {
            !question.answerOptions.isNullOrEmpty() -> {
                question.answerOptions.forEach { option ->
                    AnswerOptionCard(
                        text = option,
                        isSelected = currentAnswer == option,
                        onClick = { onAnswerSelected(option) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }
            }

            question.questionType.contains("RATING", ignoreCase = true) -> {
                StarRatingQuestion(
                    currentRating = currentAnswer.toIntOrNull() ?: 0,
                    onRatingSelected = { rating -> onAnswerSelected(rating.toString()) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            else -> {
                TextAnswerQuestion(
                    currentText = currentAnswer,
                    onTextInputRequest = { showTextInputDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )

                if (showTextInputDialog) {
                    TextInputDialog(
                        currentText = currentAnswer,
                        onTextSave = { text ->
                            onAnswerSelected(text)
                            showTextInputDialog = false
                        },
                        onDismiss = { showTextInputDialog = false }
                    )
                }
            }
        }
    }
}

@Composable
fun AnswerOptionCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF006FFD) else Color.White,
            contentColor = if (isSelected) Color.White else Color(0xFF1A1A1A)
        ),
        border = if (!isSelected) BorderStroke(
            1.dp,
            Color(0xFFE0E0E0)
        ) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StarRatingQuestion(
    currentRating: Int,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Оцените от 1 до 5 звезд",
            fontSize = 16.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (i in 1..5) {
                Icon(
                    painter = painterResource(
                        if (i <= currentRating) android.R.drawable.star_big_on
                        else android.R.drawable.star_big_off
                    ),
                    contentDescription = "$i звезд",
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onRatingSelected(i) },
                    tint = Color(0xFFFFC107)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (currentRating) {
                1 -> "Очень плохо"
                2 -> "Плохо"
                3 -> "Нормально"
                4 -> "Хорошо"
                5 -> "Отлично"
                else -> "Выберите оценку"
            },
            fontSize = 14.sp,
            color = Color(0xFF666666),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TextAnswerQuestion(
    currentText: String,
    onTextInputRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onTextInputRequest() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = if (currentText.isNotEmpty()) currentText else "Нажмите чтобы ввести ответ...",
                fontSize = 16.sp,
                color = if (currentText.isNotEmpty()) Color(0xFF1A1A1A) else Color(0xFF999999)
            )
        }
    }
}

@Composable
fun TextInputDialog(
    currentText: String,
    onTextSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var textState by remember { mutableStateOf(currentText) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Введите ваш ответ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Введите ваш ответ здесь...") },
                    singleLine = false
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF006FFD)
                        )
                    ) {
                        Text("Отмена")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onTextSave(textState) },
                        enabled = textState.isNotBlank()
                    ) {
                        Text("Сохранить")
                    }
                }
            }
        }
    }
}

@Composable
fun NavigationButtons(
    isFirstQuestion: Boolean,
    isLastQuestion: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    currentAnswer: String = "",
    modifier: Modifier = Modifier
) {
    val isAnswerSelected = currentAnswer.isNotBlank()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onPrevious,
            enabled = !isFirstQuestion,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color(0xFF006FFD)
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text("Назад")
        }

        Button(
            onClick = if (isLastQuestion) onSubmit else onNext,
            enabled = isAnswerSelected,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF006FFD)
            )
        ) {
            Text(if (isLastQuestion) "Отправить ответы врачу" else "Далее")
        }
    }
}

@Composable
fun NoQuestionsMessage(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Вопросы не найдены",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Для этого опроса пока нет вопросов",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
    }
}