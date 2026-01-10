// presentation/Screen/PatientDetailsScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bigproj.data.model.PatientAttemptDto
import com.example.bigproj.data.model.PatientDto
import com.example.bigproj.data.model.ScheduledSurveyDto
import com.example.bigproj.presentation.Screen.state.PatientDetailsEvent
import com.example.bigproj.presentation.Screen.state.PatientDetailsTab
import com.example.bigproj.presentation.Screen.viewmodel.PatientDetailsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsScreen(
    patientId: Int,
    patient: PatientDto? = null,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModel<PatientDetailsViewModel>()
    val state = viewModel.state

    // Setup dependencies
    LaunchedEffect(Unit) {
        viewModel.setupDependencies(context)
    }

    // Load data when screen opens
    LaunchedEffect(patientId) {
        viewModel.onEvent(PatientDetailsEvent.LoadPatientHistory(patientId))
        viewModel.onEvent(PatientDetailsEvent.LoadPatientAppointments(patientId))
    }

    val snackbarHostState = remember { SnackbarHostState() }

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
                        Text("Детали пациента")
                        patient?.fullName?.let {
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
                        Text("←", fontSize = 20.sp)
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
        ) {
            // Patient info section
            PatientInfoSection(patient = state.patient ?: patient)

            // Tabs
            TabRow(
                selectedTabIndex = when (state.selectedTab) {
                    PatientDetailsTab.HISTORY -> 0
                    PatientDetailsTab.APPOINTMENTS -> 1
                },
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                indicator = { tabPositions ->
                    val selectedIndex = when (state.selectedTab) {
                        PatientDetailsTab.HISTORY -> 0
                        PatientDetailsTab.APPOINTMENTS -> 1
                    }
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedIndex])
                    )
                }
            ) {
                Tab(
                    selected = state.selectedTab == PatientDetailsTab.HISTORY,
                    onClick = { viewModel.selectTab(PatientDetailsTab.HISTORY) },
                    text = { Text("История") },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Tab(
                    selected = state.selectedTab == PatientDetailsTab.APPOINTMENTS,
                    onClick = { viewModel.selectTab(PatientDetailsTab.APPOINTMENTS) },
                    text = { Text("Назначения") },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Tab content
            when (state.selectedTab) {
                PatientDetailsTab.HISTORY -> {
                    HistoryTabContent(
                        history = state.history,
                        isLoading = state.isLoadingHistory
                    )
                }
                PatientDetailsTab.APPOINTMENTS -> {
                    AppointmentsTabContent(
                        appointments = state.appointments,
                        isLoading = state.isLoadingAppointments
                    )
                }
            }
        }
    }
}

@Composable
fun PatientInfoSection(patient: PatientDto?) {
    if (patient == null) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2196F3)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getInitials(patient.fullName ?: patient.email),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = patient.fullName ?: "Не указано",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = patient.email,
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Зарегистрирован с ${formatDate(patient.creationDate)}",
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
            }
        }
    }
}

@Composable
fun HistoryTabContent(
    history: List<PatientAttemptDto>,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (history.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "История опросов пуста",
                fontSize = 16.sp,
                color = Color(0xFF666666)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(history) { attempt ->
                HistoryAttemptCard(attempt = attempt)
            }
        }
    }
}

@Composable
fun HistoryAttemptCard(attempt: PatientAttemptDto) {
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
                                maxLines = 2,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .background(
                            when (attempt.status) {
                                "completed" -> Color(0xFFE8F5E9)
                                "in_progress" -> Color(0xFFFFF3E0)
                                else -> Color(0xFFF5F5F5)
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (attempt.status) {
                            "completed" -> "Завершен"
                            "in_progress" -> "В процессе"
                            else -> attempt.status
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = when (attempt.status) {
                            "completed" -> Color(0xFF2E7D32)
                            "in_progress" -> Color(0xFFF57C00)
                            else -> Color(0xFF666666)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Дата: ${attempt.creationDate.take(10)}",
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Ответов: ${attempt.answers.size}",
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
            }
        }
    }
}

@Composable
fun AppointmentsTabContent(
    appointments: List<ScheduledSurveyDto>,
    isLoading: Boolean
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (appointments.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Нет активных назначений",
                fontSize = 16.sp,
                color = Color(0xFF666666)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(appointments) { appointment ->
                AppointmentCard(appointment = appointment)
            }
        }
    }
}

@Composable
fun AppointmentCard(appointment: ScheduledSurveyDto) {
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
                        text = appointment.title ?: "Опрос #${appointment.surveyId}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    appointment.description?.let { description ->
                        if (description.isNotBlank()) {
                            Text(
                                text = description,
                                fontSize = 12.sp,
                                color = Color(0xFF666666),
                                maxLines = 2,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .background(
                            if (appointment.isActive == true) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (appointment.isActive == true) "Активно" else "Неактивно",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (appointment.isActive == true) Color(0xFF2E7D32) else Color(0xFF666666)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            appointment.frequencyType?.let { frequency ->
                Text(
                    text = "Частота: $frequency",
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
            }
            
            appointment.startDate?.let { startDate ->
                Text(
                    text = "Начало: ${startDate.take(10)}",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            appointment.nextScheduledDate?.let { nextDate ->
                Text(
                    text = "Следующее: ${nextDate.take(10)}",
                    fontSize = 12.sp,
                    color = Color(0xFF006FFD),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            appointment.scheduledTimes?.takeIf { it.isNotEmpty() }?.let { times ->
                Text(
                    text = "Время: ${times.joinToString(", ")}",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
