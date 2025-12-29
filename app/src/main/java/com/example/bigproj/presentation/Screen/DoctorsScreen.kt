// presentation/Screen/DoctorsScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bigproj.data.model.PatientDto
import com.example.bigproj.presentation.Screen.state.DoctorScreenEvent
import com.example.bigproj.presentation.Screen.state.DoctorScreenState
import com.example.bigproj.presentation.Screen.viewmodel.DoctorViewModel
import kotlinx.coroutines.delay

enum class DoctorTab {
    SURVEYS,
    PATIENTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorsScreen(
    navController: NavHostController,
    onNavigateToCreateSurvey: () -> Unit = {},
    onNavigateToManageSurveys: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel = viewModel<DoctorViewModel>()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(DoctorTab.SURVEYS) }

    LaunchedEffect(Unit) {
        viewModel.setupDependencies(context)
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == DoctorTab.PATIENTS) {
            viewModel.onEvent(DoctorScreenEvent.LoadPatients)
        }
    }

    val errorMessage = viewModel.state.errorMessage
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
            delay(3000)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Панель врача") }
            )
        },
        floatingActionButton = {
            if (selectedTab == DoctorTab.SURVEYS) {
                FloatingActionButton(
                    onClick = onNavigateToCreateSurvey,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ИСПРАВЛЕНО: Используем TabRow вместо PrimaryTabRow
            TabRow(
                selectedTabIndex = when (selectedTab) {
                    DoctorTab.SURVEYS -> 0
                    DoctorTab.PATIENTS -> 1
                },
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                indicator = { tabPositions ->
                    val selectedIndex = when (selectedTab) {
                        DoctorTab.SURVEYS -> 0
                        DoctorTab.PATIENTS -> 1
                    }
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedIndex])
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == DoctorTab.SURVEYS,
                    onClick = { selectedTab = DoctorTab.SURVEYS },
                    text = { Text("Мои опросы") },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Tab(
                    selected = selectedTab == DoctorTab.PATIENTS,
                    onClick = { selectedTab = DoctorTab.PATIENTS },
                    text = { Text("Мои пациенты") },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when (selectedTab) {
                DoctorTab.PATIENTS -> {
                    DoctorsPatientsListContent(
                        state = viewModel.state,
                        onPatientClick = { patient: PatientDto ->
                            viewModel.onEvent(DoctorScreenEvent.LoadPatientAttempts(patient.id))
                        },
                        onScheduleClick = { patientId ->
                            navController.navigate("schedule_survey/$patientId")
                        },
                        modifier = Modifier.padding(16.dp)
                    )
                }
                DoctorTab.SURVEYS -> {
                    DoctorsSurveysContent(
                        state = viewModel.state,
                        onCreateSurvey = onNavigateToCreateSurvey,
                        onManageSurveys = onNavigateToManageSurveys,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DoctorsSurveysContent(
    state: DoctorScreenState,
    onCreateSurvey: () -> Unit,
    onManageSurveys: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onCreateSurvey,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Создать опрос", fontSize = 14.sp)
            }

            Button(
                onClick = onManageSurveys,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Все опросы", fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Последние опросы",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("📋", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Создайте ваш первый опрос",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF444444),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun DoctorsPatientsListContent(
    state: DoctorScreenState,
    onPatientClick: (PatientDto) -> Unit,
    onScheduleClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Мои пациенты",
            fontSize = 18.sp,
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
            EmptyDoctorsPatientsState()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.patients) { patient ->
                    DoctorsPatientCard(
                        patient = patient,
                        onClick = { onPatientClick(patient) },
                        onSchedule = { onScheduleClick(patient.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyDoctorsPatientsState() {
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
            Text("👥", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Пациенты не найдены",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF444444),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Пациенты появятся здесь после их регистрации и привязки",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DoctorsPatientCard(
    patient: PatientDto,
    onClick: () -> Unit,
    onSchedule: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                        text = patient.fullName ?: "Без имени",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Text(
                        text = patient.email,
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (patient.isActive) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (patient.isActive) "Активен" else "Неактивен",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (patient.isActive) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("ID: ${patient.id}", fontSize = 12.sp, color = Color(0xFF888888))
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Зарегистрирован: ${patient.creationDate.take(10)}",
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onSchedule,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Назначить опрос", fontSize = 12.sp)
                }
            }
        }
    }
}