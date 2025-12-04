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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.navigation.NavHostController
import com.example.bigproj.presentation.navigation.Screen
import kotlinx.coroutines.delay
import com.example.bigproj.presentation.Screen.PatientCard
import com.example.bigproj.presentation.Screen.PatientsListContent

enum class DoctorTab {
    PATIENTS, SURVEYS, STATISTICS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorsScreen(
    navController: NavHostController,
    onNavigateToCreateSurvey: () -> Unit = { navController.navigate(Screen.CreateSurvey) },
    onNavigateToManageSurveys: () -> Unit = { navController.navigate(Screen.ManageSurveys) } // ← ДОБАВЛЕНО
) {
    val context = LocalContext.current
    val viewModel = viewModel<com.example.bigproj.presentation.Screen.viewmodel.DoctorViewModel>()

    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(DoctorTab.PATIENTS) }

    LaunchedEffect(Unit) {
        viewModel.setupDependencies(context)
    }

    // Показываем ошибки
    val errorMessage = viewModel.state.errorMessage
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = androidx.compose.material3.SnackbarDuration.Short
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Быстрые действия
            QuickActionsPanel(
                onCreateSurvey = onNavigateToCreateSurvey,
                onViewPatients = { selectedTab = DoctorTab.PATIENTS },
                onViewStatistics = { selectedTab = DoctorTab.STATISTICS },
                onManageSurveys = onNavigateToManageSurveys // ← ПЕРЕДАЁМ НОВЫЙ ПАРАМЕТР
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Вкладки
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    text = { Text("Пациенты") },
                    selected = selectedTab == DoctorTab.PATIENTS,
                    onClick = { selectedTab = DoctorTab.PATIENTS }
                )
                Tab(
                    text = { Text("Опросы") },
                    selected = selectedTab == DoctorTab.SURVEYS,
                    onClick = { selectedTab = DoctorTab.SURVEYS }
                )
                Tab(
                    text = { Text("Статистика") },
                    selected = selectedTab == DoctorTab.STATISTICS,
                    onClick = { selectedTab = DoctorTab.STATISTICS }
                )
            }

            // Контент вкладок
            when (selectedTab) {
                DoctorTab.PATIENTS -> {
                    PatientsListContent(
                        state = viewModel.state,
                        onPatientClick = { patient: com.example.bigproj.data.model.PatientDto ->
                            // Навигация к ответам пациента
                        },
                        modifier = Modifier.padding(16.dp)
                    )
                }
                DoctorTab.SURVEYS -> {
                    DoctorSurveysContent(
                        state = viewModel.state,
                        onCreateSurvey = onNavigateToCreateSurvey,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                DoctorTab.STATISTICS -> {
                    StatisticsContent(
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsPanel(
    onCreateSurvey: () -> Unit,
    onViewPatients: () -> Unit,
    onViewStatistics: () -> Unit,
    onManageSurveys: () -> Unit, // ← ДОБАВЛЕНО
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Быстрые действия",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    text = "Создать опрос",
                    emoji = "📝",
                    onClick = onCreateSurvey,
                    modifier = Modifier.weight(1f)
                )

                QuickActionButton(
                    text = "Мои опросы", // ← НОВАЯ КНОПКА
                    emoji = "📋",
                    onClick = onManageSurveys,
                    modifier = Modifier.weight(1f)
                )

                QuickActionButton(
                    text = "Мои пациенты",
                    emoji = "👥",
                    onClick = onViewPatients,
                    modifier = Modifier.weight(1f)
                )

                QuickActionButton(
                    text = "Статистика",
                    emoji = "📊",
                    onClick = onViewStatistics,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    text: String,
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DoctorSurveysContent(
    state: com.example.bigproj.presentation.Screen.state.DoctorScreenState,
    onCreateSurvey: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Кнопка создания нового опроса
        Button(
            onClick = onCreateSurvey,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("📝 Создать новый опрос", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Разделитель
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFEEEEEE))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Заголовок
        Text(
            text = "Мои опросы",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // TODO: Добавить список опросов врача
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
                    Text(
                        text = "📋",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Создайте свой первый опрос",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF444444),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Нажмите кнопку 'Создать новый опрос' выше",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun StatisticsContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                Text(
                    text = "📊",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Статистика в разработке",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF444444),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Этот раздел будет доступен в ближайшем обновлении",
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
