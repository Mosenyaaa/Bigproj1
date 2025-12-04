// presentation/Screen/PatientScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bigproj.presentation.Screen.state.DoctorScreenEvent
import com.example.bigproj.presentation.Screen.state.DoctorView
import com.example.bigproj.presentation.Screen.viewmodel.DoctorViewModel
import kotlinx.coroutines.delay

@Composable
fun PatientsScreen() {
    val viewModel = viewModel<DoctorViewModel>()
    val state = viewModel.state

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.onEvent(DoctorScreenEvent.LoadPatients)
    }

    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = state.errorMessage,
                duration = androidx.compose.material3.SnackbarDuration.Short
            )
            delay(3000)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (state.currentView) {
            DoctorView.PATIENTS_LIST -> {
                PatientsListContent(
                    state = state,
                    onPatientClick = { patient ->
                        viewModel.onEvent(DoctorScreenEvent.PatientSelected(patient.id))
                        viewModel.onEvent(DoctorScreenEvent.LoadPatientAttempts(patient.id))
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            DoctorView.PATIENT_ATTEMPTS -> {
                PatientAttemptsScreen(
                    patientId = state.selectedPatientId ?: 0,
                    patientName = state.patients.find { it.id == state.selectedPatientId }?.fullName,
                    onBackClick = { viewModel.onEvent(DoctorScreenEvent.NavigateBack) }
                )
            }
        }
    }
}

@Composable
fun PatientsListContent(
    state: com.example.bigproj.presentation.Screen.state.DoctorScreenState,
    onPatientClick: (com.example.bigproj.data.model.PatientDto) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Мои пациенты",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.patients.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Пациенты не найдены",
                    fontSize = 16.sp,
                    color = Color(0xFF666666)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.patients) { patient ->
                    PatientCard(
                        patient = patient,
                        onPatientClick = onPatientClick
                    )
                }
            }
        }
    }
}

@Composable
fun PatientCard(
    patient: com.example.bigproj.data.model.PatientDto,
    onPatientClick: (com.example.bigproj.data.model.PatientDto) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPatientClick(patient) },
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = patient.fullName ?: "Не указано",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Text(
                text = patient.email,
                fontSize = 14.sp,
                color = Color(0xFF666666),
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = if (patient.isVerified) "✓ Подтвержден" else "⏳ Ожидает подтверждения",
                fontSize = 12.sp,
                color = if (patient.isVerified) Color(0xFF00C853) else Color(0xFFFF9800),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}