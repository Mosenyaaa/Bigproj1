// presentation/Screen/main/MainScreenWithBottomNav.kt
package com.example.bigproj.presentation.Screen.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.bigproj.presentation.Screen.PatientDoctorsScreen
import com.example.bigproj.presentation.Screen.state.MainScreenEvent
import com.example.bigproj.presentation.Screen.viewmodel.MainScreenViewModel
import com.example.bigproj.presentation.navigation.Screen

@Composable
fun MainScreenWithBottomNav(
    navController: NavHostController,
    onNavigateTo: (Screen) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = viewModel<MainScreenViewModel>()

    // Load user data to determine role
    LaunchedEffect(Unit) {
        viewModel.setupDependencies(context)
        viewModel.onEvent(MainScreenEvent.LoadUserData)
    }

    val isDoctor = viewModel.state.isDoctor

    // Different nav items for doctors and patients
    val patientNavItems = listOf(
        NavigationItem.Surveys,
        NavigationItem.Doctors,
        NavigationItem.Profile
    )

    val doctorNavItems = listOf(
        NavigationItem.Patients,
        NavigationItem.DoctorSurveys,
        NavigationItem.Constructor,
        NavigationItem.Appointments,
        NavigationItem.DoctorProfile
    )

    val bottomNavItems = if (isDoctor) doctorNavItems else patientNavItems

    // State for selected tab
    var selectedItem by rememberSaveable { mutableStateOf(0) }

    // Track previous role to reset tab only when role actually changes
    var previousRole by remember { mutableStateOf<Boolean?>(null) }

    // Reset to first tab when role changes (but not on initial load)
    LaunchedEffect(isDoctor) {
        if (previousRole != null && previousRole != isDoctor) {
            selectedItem = 0
        }
        previousRole = isDoctor
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.height(70.dp),
                tonalElevation = 0.dp,
                windowInsets = NavigationBarDefaults.windowInsets
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            // ИКОНКА
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            // НАЗВАНИЕ ПОД ИКОНКОЙ
                            Text(
                                text = item.title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedItem == index) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF006FFD),
                            unselectedIconColor = Color(0xFF666666),
                            selectedTextColor = Color(0xFF006FFD),
                            unselectedTextColor = Color(0xFF666666),
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { paddingValues ->
        // Показать разные экраны в зависимости от роли и выбранной вкладки
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isDoctor) {
                when (selectedItem) {
                    0 -> com.example.bigproj.presentation.Screen.PatientsScreen(navController = navController)
                    1 -> com.example.bigproj.presentation.Screen.DoctorSurveysScreen(navController = navController)
                    2 -> com.example.bigproj.presentation.Screen.ConstructorScreen(navController = navController)
                    3 -> com.example.bigproj.presentation.Screen.AppointmentsScreen(navController = navController)
                    4 -> MainScreen(onNavigateTo = onNavigateTo, navController = navController)
                }
            } else {
                when (selectedItem) {
                    0 -> com.example.bigproj.presentation.Screen.SurveyListScreen(
                        onNavigateToSurvey = { surveyId ->
                            navController.navigate("survey_detail/$surveyId")
                        },
                        onNavigateToMain = {
                            navController.navigate(Screen.Main)
                        }
                    )
                    1 -> PatientDoctorsScreen()
                    2 -> MainScreen(onNavigateTo = onNavigateTo, navController = navController)
                }
            }
        }
    }
}

// Объекты для навигации (новые чтобы избежать конфликта)
sealed class NavigationItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    // Patient items
    object Surveys : NavigationItem("surveys", "Опросы", Icons.Filled.QuestionAnswer)
    object Doctors : NavigationItem("doctors", "Врачи", Icons.Filled.MedicalServices)
    object Profile : NavigationItem("profile", "Профиль", Icons.Filled.Person)

    // Doctor items
    object Patients : NavigationItem("patients", "Пациенты", Icons.Filled.Group)
    object DoctorSurveys : NavigationItem("doctor_surveys", "Опросы", Icons.Filled.Assignment)
    object Constructor : NavigationItem("constructor", "Конструктор", Icons.Filled.Build)
    object Appointments : NavigationItem("appointments", "Назначения", Icons.Filled.CalendarToday)
    object DoctorProfile : NavigationItem("doctor_profile", "Профиль", Icons.Filled.AccountCircle)
}