// presentation/Screen/main/MainScreenWithBottomNav.kt
package com.example.bigproj.presentation.Screen.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bigproj.presentation.Screen.PatientDoctorsScreen
import com.example.bigproj.presentation.Screen.PatientsScreen
import com.example.bigproj.presentation.Screen.state.MainScreenEvent
import com.example.bigproj.presentation.Screen.viewmodel.MainScreenViewModel
import com.example.bigproj.presentation.navigation.Screen

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    // Patient items
    object Surveys : BottomNavItem("surveys", "Опросы", Icons.Filled.QuestionAnswer)
    object Doctors : BottomNavItem("doctors", "Врачи", Icons.Filled.MedicalServices)
    object Profile : BottomNavItem("profile", "Профиль", Icons.Filled.Person)

    // Doctor items
    object Patients : BottomNavItem("patients", "Пациенты", Icons.Filled.Group)
    object DoctorSurveys : BottomNavItem("doctor_surveys", "Опросы", Icons.Filled.Assignment)
    object Constructor : BottomNavItem("constructor", "Конструктор", Icons.Filled.Build)
    object Appointments : BottomNavItem("appointments", "Назначения", Icons.Filled.CalendarToday)
    object DoctorProfile : BottomNavItem("doctor_profile", "Профиль", Icons.Filled.AccountCircle)
}

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
        BottomNavItem.Surveys,
        BottomNavItem.Doctors,
        BottomNavItem.Profile
    )

    val doctorNavItems = listOf(
        BottomNavItem.Patients,
        BottomNavItem.DoctorSurveys,
        BottomNavItem.Constructor,
        BottomNavItem.Appointments,
        BottomNavItem.DoctorProfile
    )

    val bottomNavItems = if (isDoctor) doctorNavItems else patientNavItems

    // Start on different tab based on role
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
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.height(64.dp),
                tonalElevation = 8.dp
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { paddingValues ->
        // Show different screens based on role and selected tab
        if (isDoctor) {
            // Doctor navigation
            when (selectedItem) {
                0 -> {
                    // Patients - index 0 for doctors
                    PatientsScreen(navController = navController)
                }
                1 -> {
                    // Surveys
                    com.example.bigproj.presentation.Screen.SurveyListScreen(
                        onNavigateToSurvey = { surveyId ->
                            navController.navigate("survey_detail/$surveyId")
                        },
                        onNavigateToMain = {
                            navController.navigate(Screen.Main)
                        }
                    )
                }
                2 -> {
                    // Constructor (Questions Constructor)
                    com.example.bigproj.presentation.Screen.ConstructorScreen(
                        navController = navController
                    )
                }
                3 -> {
                    // Appointments
                    com.example.bigproj.presentation.Screen.AppointmentsScreen(
                        navController = navController
                    )
                }
                4 -> {
                    // Profile
                    MainScreen(
                        onNavigateTo = onNavigateTo,
                        navController = navController
                    )
                }
            }
        } else {
            // Patient navigation
            when (selectedItem) {
                0 -> {
                    // Surveys - index 0 for patients
                    com.example.bigproj.presentation.Screen.SurveyListScreen(
                        onNavigateToSurvey = { surveyId ->
                            navController.navigate("survey_detail/$surveyId")
                        },
                        onNavigateToMain = {
                            navController.navigate(Screen.Main)
                        }
                    )
                }
                1 -> {
                    // Doctors
                    PatientDoctorsScreen()
                }
                2 -> {
                    // Profile
                    MainScreen(
                        onNavigateTo = onNavigateTo,
                        navController = navController
                    )
                }
            }
        }
    }
}
