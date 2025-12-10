// presentation/Screen/PatientAttemptsScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bigproj.data.model.PatientAttemptDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientAttemptsScreen(
    patientId: Int,
    patientName: String?,
    attempts: List<PatientAttemptDto>, // ИСПРАВЛЕНО: List<PatientAttemptDto> вместо List<Any>
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Ответы пациента")
                        patientName?.let {
                            Text(
                                text = it,
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (attempts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("У пациента нет пройденных опросов")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(attempts) { attempt ->
                    PatientAttemptCard(attempt = attempt)
                }
            }
        }
    }
}

@Composable
fun PatientAttemptCard(attempt: PatientAttemptDto) { // ИСПРАВЛЕНО: PatientAttemptDto вместо Any
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = attempt.surveyTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )

                    attempt.surveyDescription?.let { description ->
                        if (description.isNotBlank()) {
                            Text(
                                text = description,
                                fontSize = 12.sp,
                                color = Color(0xFF666666),
                                maxLines = 2
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (attempt.status) {
                                "completed" -> Color(0xFFE8F5E9)
                                "started" -> Color(0xFFFFF3E0)
                                else -> Color(0xFFF5F5F5)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (attempt.status) {
                            "completed" -> "Завершен"
                            "started" -> "Начат"
                            else -> attempt.status
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (attempt.status) {
                            "completed" -> Color(0xFF2E7D32)
                            "started" -> Color(0xFFF57C00)
                            else -> Color(0xFF666666)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ID попытки: ${attempt.attemptId}",
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Дата: ${attempt.creationDate.take(10)}",
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ответов: ${attempt.answers.size}",
                fontSize = 12.sp,
                color = Color(0xFF888888)
            )
        }
    }
}