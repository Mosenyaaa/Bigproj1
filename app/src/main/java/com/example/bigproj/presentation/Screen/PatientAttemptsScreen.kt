// presentation/Screen/PatientAttemptsScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bigproj.presentation.Screen.viewmodel.DoctorViewModel

@Composable
fun PatientAttemptsScreen(
    patientId: Int,
    patientName: String? = null,
    onBackClick: () -> Unit = {},
    viewModel: DoctorViewModel = viewModel()
) {
    val state = viewModel.state

    // 🔥 ВРЕМЕННЫЕ ТЕСТОВЫЕ ДАННЫЕ (ДОБАВЛЕНО)
    val testAttempts = remember(patientId) {
        if (state.patientAttempts.isEmpty()) {
            listOf(
                com.example.bigproj.data.model.PatientAttemptDto(
                    attemptId = 999,
                    surveyId = 2,
                    surveyTitle = "Тестовый опрос самочувствия",
                    surveyDescription = "Проверка отображения данных",
                    status = "completed",
                    creationDate = "2025-11-27T12:00:00Z",
                    answers = listOf(
                        com.example.bigproj.data.model.PatientAnswerDto(
                            answerId = 1,
                            questionInSurveyId = 1,
                            questionText = "Как вы себя чувствуете?",
                            questionType = "text",
                            orderIndex = 1,
                            text = "Отлично, спасибо!",
                            voiceFilename = null,
                            pictureFilename = null,
                            creationDate = "2025-11-27T12:00:00Z"
                        )
                    )
                )
            )
        } else {
            emptyList()
        }
    }

    val displayAttempts = if (state.patientAttempts.isEmpty()) testAttempts else state.patientAttempts
    val isTestData = state.patientAttempts.isEmpty() && testAttempts.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Кнопка назад
        Button(
            onClick = onBackClick,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("← Назад к списку пациентов")
        }

        Text(
            text = patientName?.let { "Ответы пациента: $it" } ?: "Ответы пациента",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isTestData) {
            Text(
                "⚠️ Тестовые данные (реальные данные не найдены)",
                color = Color.Yellow,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        PatientAttemptsContent(
            state = state.copy(patientAttempts = displayAttempts),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun PatientAttemptsContent(
    state: com.example.bigproj.presentation.Screen.state.DoctorScreenState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.patientAttempts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Пациент еще не прошел ни одного опроса",
                    fontSize = 16.sp,
                    color = Color(0xFF666666)
                )
            }
        } else {
            Text(
                text = "Пройденные опросы:",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.patientAttempts) { attempt ->
                    PatientAttemptCard(attempt = attempt)
                }
            }
        }
    }
}

@Composable
fun PatientAttemptCard(attempt: com.example.bigproj.data.model.PatientAttemptDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = attempt.surveyTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            attempt.surveyDescription?.let { description ->
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Text(
                text = "Статус: ${attempt.status}",
                fontSize = 12.sp,
                color = when (attempt.status) {
                    "completed" -> Color(0xFF00C853)
                    "started" -> Color(0xFFFF9800)
                    else -> Color(0xFF666666)
                },
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Дата: ${attempt.creationDate}",
                fontSize = 12.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "Ответов: ${attempt.answers.size}",
                fontSize = 12.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(top = 4.dp)
            )

            // 🔥 ПОКАЗЫВАЕМ ОТВЕТЫ ЕСЛИ ОНИ ЕСТЬ
            if (attempt.answers.isNotEmpty()) {
                Text(
                    text = "Ответы:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                )

                attempt.answers.forEachIndexed { index, answer ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Вопрос: ${answer.questionText}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1A1A1A)
                            )

                            answer.text?.let { textAnswer ->
                                if (textAnswer.isNotBlank()) {
                                    Text(
                                        text = "Ответ: $textAnswer",
                                        fontSize = 13.sp,
                                        color = Color(0xFF444444),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Тип: ${answer.questionType}",
                                fontSize = 11.sp,
                                color = Color(0xFF666666),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}