package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bigproj.presentation.Screen.viewmodel.ScheduleSurveyViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSurveyScreen(
    patientId: Int,
    onBack: () -> Unit = {}
) {
    val viewModel: ScheduleSurveyViewModel = viewModel()
    val context = LocalContext.current
    val state = viewModel.state
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(patientId) {
        viewModel.setup(context, patientId)
        viewModel.loadSchedules()
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            delay(2500)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Планирование опроса") },
                navigationIcon = {
                    Button(onClick = onBack, modifier = Modifier.padding(start = 8.dp)) {
                        Text("Назад")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Пациент ID: $patientId", fontWeight = FontWeight.SemiBold)

            OutlinedTextField(
                value = state.surveyId,
                onValueChange = { viewModel.updateSurveyId(it) },
                label = { Text("ID опроса") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.updateTitle(it) },
                label = { Text("Заголовок напоминания (опц.)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.startDate,
                onValueChange = { viewModel.updateStartDate(it) },
                label = { Text("Дата начала (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.time,
                onValueChange = { viewModel.updateTime(it) },
                label = { Text("Время (HH:mm:ss)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.maxReminders,
                    onValueChange = { viewModel.updateMaxReminders(it) },
                    label = { Text("Макс. напоминаний") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.reminderIntervalMinutes,
                    onValueChange = { viewModel.updateReminderInterval(it) },
                    label = { Text("Интервал, мин") },
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = { viewModel.schedule() },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canSend && !state.isLoading
            ) {
                Text(if (state.isLoading) "Сохранение..." else "Запланировать")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Запланированные опросы", fontWeight = FontWeight.Bold)

            if (state.scheduled.isEmpty()) {
                Text("Пока нет запланированных опросов", color = Color.Gray)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.scheduled) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(item.title ?: "Без названия", fontWeight = FontWeight.SemiBold)
                                Text("Опрос ID: ${item.surveyId}", color = Color.DarkGray)
                                item.nextScheduledDate?.let { Text("Следующая дата: $it") }
                                item.scheduledTimes?.let { times ->
                                    Text("Время: ${times.joinToString()}", color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
