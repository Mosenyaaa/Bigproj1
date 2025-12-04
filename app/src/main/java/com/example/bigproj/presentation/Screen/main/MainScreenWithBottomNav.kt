// presentation/Screen/main/MainScreenWithBottomNav.kt
package com.example.bigproj.presentation.Screen.main

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bigproj.presentation.Screen.PatientsScreen
import com.example.bigproj.presentation.Screen.SurveyListScreen
import com.example.bigproj.presentation.Screen.viewmodel.DoctorViewModel
import com.example.bigproj.presentation.navigation.Screen

sealed class BottomNavItem(
    val route: String,
    val title: String
) {
    object Surveys : BottomNavItem("surveys", "Опросы")
    object Doctors : BottomNavItem("doctors", "Врачи")
    object Settings : BottomNavItem("settings", "Настройки")
}

@Composable
fun MainScreenWithBottomNav(
    navController: NavHostController,
    onNavigateTo: (Screen) -> Unit = {}
) {
    val bottomNavItems = listOf(
        BottomNavItem.Surveys,
        BottomNavItem.Doctors,
        BottomNavItem.Settings
    )

    var selectedItem by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { }, // 🔥 БЕЗ ИКОНОК
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 12.sp
                            )
                        },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        when (selectedItem) {
            0 -> SurveyListScreen(
                onNavigateToSurvey = { surveyId ->
                    onNavigateTo(Screen.SurveyDetail(surveyId))
                },
                onNavigateToMain = {
                    // Остаемся на этом экране
                }
            )
            1 -> DoctorsScreen() // 🔥 ПУСТОЙ ЭКРАН ВРАЧЕЙ
            2 -> MainScreen(     // 🔥 СТАРЫЙ МЕЙН СКРИН В НАСТРОЙКАХ
                onNavigateTo = onNavigateTo
            )
        }
    }
}

// 🔥 ПУСТОЙ ЭКРАН ДЛЯ ВРАЧЕЙ
@Composable
fun DoctorsScreen() {
    val context = LocalContext.current
    val viewModel = viewModel<DoctorViewModel>()

    LaunchedEffect(Unit) {
        viewModel.setupDependencies(context)
    }

    PatientsScreen()
}