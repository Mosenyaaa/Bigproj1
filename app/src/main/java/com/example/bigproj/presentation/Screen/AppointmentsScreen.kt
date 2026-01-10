// presentation/Screen/AppointmentsScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bigproj.presentation.Screen.state.AppointmentsEvent
import com.example.bigproj.presentation.Screen.viewmodel.AppointmentsViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(
    navController: NavHostController? = null
) {
    val context = LocalContext.current
    val viewModel = viewModel<AppointmentsViewModel>()
    val state = viewModel.state

    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf<com.example.bigproj.data.model.ScheduledSurveyDto?>(null) }

    LaunchedEffect(Unit) {
        viewModel.setupDependencies(context)
        viewModel.onEvent(AppointmentsEvent.LoadAppointments)
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
                            text = "Назначения",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (!state.isLoading) {
                            Text(
                                text = "${state.activeAppointmentsCount} активных",
                                fontSize = 12.sp,
                                color = Color(0xFF666666)
                            )
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
                .padding(16.dp)
        ) {
            // Button to create appointment - УЛУЧШЕННЫЙ ВИД
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        navController?.navigate("create_appointment")
                    }
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Назначить новый опрос",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Создать назначение для пациента",
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                    }
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = Color(0xFF666666)
                    )
                }
            }
        }

        // Delete confirmation dialog
        showDeleteDialog?.let { appointmentToDelete ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Удалить назначение?") },
                text = {
                    Text("Вы уверены, что хотите удалить назначение \"${appointmentToDelete.title ?: "Без названия"}\"?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.onEvent(AppointmentsEvent.DeleteAppointment(appointmentToDelete.id))
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

@Composable
fun AppointmentCard(
    appointmentWithPatient: com.example.bigproj.presentation.Screen.state.ScheduledSurveyWithPatient,
    onPauseClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val appointment = appointmentWithPatient.appointment
    val patient = appointmentWithPatient.patient

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row with icon, title, and delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Document icon
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                Color(0xFFE3F2FD),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📄", fontSize = 18.sp)
                    }
                    // Title
                    Text(
                        text = appointment.title ?: "Без названия",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        modifier = Modifier.weight(1f)
                    )
                }
                // Delete button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text("🗑️", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Patient name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("👤", fontSize = 16.sp)
                Text(
                    text = patient?.fullName ?: "Неизвестный пациент",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Frequency and start date badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Frequency badge
                Badge(
                    containerColor = Color(0xFFF5F5F5),
                    contentColor = Color(0xFF666666)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("🕐", fontSize = 12.sp)
                        Text(
                            text = formatFrequency(appointment.frequencyType),
                            fontSize = 12.sp
                        )
                    }
                }

                // Start date badge
                appointment.startDate?.let { startDate ->
                    Badge(
                        containerColor = Color(0xFFF5F5F5),
                        contentColor = Color(0xFF666666)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("📅", fontSize = 12.sp)
                            Text(
                                text = "с ${formatDateForAppointment(startDate)}",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pause button
            OutlinedButton(
                onClick = onPauseClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF666666)
                )
            ) {
                Text(
                    text = if (appointment.isActive == true) "Приостановить" else "Возобновить",
                    fontSize = 14.sp
                )
            }
        }
    }
}

private fun formatFrequency(frequencyType: String?): String {
    return when (frequencyType?.lowercase()) {
        "daily" -> "Ежедневно"
        "weekly" -> "Еженедельно"
        "monthly" -> "Ежемесячно"
        "once" -> "Однократно"
        else -> frequencyType ?: "Не указано"
    }
}

private fun formatDateForAppointment(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMM yyyy г.", Locale("ru", "RU"))
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}
