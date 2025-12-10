// presentation/Screen/ManageSurveysScreen.kt
package com.example.bigproj.presentation.Screen

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.example.bigproj.domain.repository.DoctorRepository
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSurveysScreen(
    onBackClick: () -> Unit = {},
    onEditSurvey: (Int) -> Unit = {},
    onCreateSurvey: () -> Unit = {}
) {
    val context = LocalContext.current
    val doctorRepository = remember { DoctorRepository(context) }

    var isLoading by remember { mutableStateOf(true) }
    var surveys by remember { mutableStateOf<List<com.example.bigproj.data.model.SurveySimpleDto>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Загружаем опросы врача
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            val response = doctorRepository.getDoctorSurveys()
            surveys = response.surveys

            // Добавляем диагностику
            println("✅ Успешно загружено опросов: ${surveys.size}")
            surveys.forEach { survey ->
                println("📋 Опрос: ID=${survey.id}, Title='${survey.title}', Status='${survey.status}'")
            }

            isLoading = false
        } catch (e: Exception) {
            println("❌ Ошибка загрузки опросов: ${e.message}")
            errorMessage = "Ошибка загрузки опросов: ${e.message}"
            isLoading = false
        }
    }

    // Показываем ошибки
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage!!,
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            delay(3000)
            errorMessage = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои опросы") },
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
        },
        floatingActionButton = {
            Button(
                onClick = onCreateSurvey,
                modifier = Modifier.padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("+ Новый опрос")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        ManageSurveysContent(
            isLoading = isLoading,
            surveys = surveys,
            onEditSurvey = onEditSurvey,
            onCreateSurvey = onCreateSurvey,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun ManageSurveysContent(
    isLoading: Boolean,
    surveys: List<com.example.bigproj.data.model.SurveySimpleDto>,
    onEditSurvey: (Int) -> Unit,
    onCreateSurvey: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Мои опросы",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (surveys.isEmpty()) {
            EmptySurveysState(onCreateSurvey = onCreateSurvey)
        } else {
            SurveysList(
                surveys = surveys,
                onEditSurvey = onEditSurvey,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun EmptySurveysState(
    onCreateSurvey: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(16.dp),
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
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text("📋", fontSize = 36.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "У вас пока нет опросов",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Создайте первый опрос для ваших пациентов",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onCreateSurvey,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Создать первый опрос", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun SurveysList(
    surveys: List<com.example.bigproj.data.model.SurveySimpleDto>,
    onEditSurvey: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(surveys) { survey ->
            SurveyItem(
                survey = survey,
                onEditClick = { onEditSurvey(survey.id) }
            )
        }
    }
}

@Composable
fun SurveyItem(
    survey: com.example.bigproj.data.model.SurveySimpleDto,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Заголовок и статус
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = survey.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    modifier = Modifier.weight(1f)
                )

                // Бейдж статуса
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (survey.status) {
                                "active" -> Color(0xFFE8F5E9)
                                "draft" -> Color(0xFFFFF3E0)
                                else -> Color(0xFFF5F5F5)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (survey.status) {
                            "active" -> "Активный"
                            "draft" -> "Черновик"
                            else -> survey.status
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (survey.status) {
                            "active" -> Color(0xFF2E7D32)
                            "draft" -> Color(0xFFF57C00)
                            else -> Color(0xFF666666)
                        }
                    )
                }
            }

            // Описание
            survey.description?.let { description ->
                if (description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            // Дополнительная информация
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: ${survey.id}",
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Дата: ${survey.creationDate.take(10)}",
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
            }

            // Кнопки действий
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onEditClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Редактировать", fontSize = 12.sp)
                }
            }
        }
    }
}