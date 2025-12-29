// presentation/Screen/main/MainScreenWithBottomNav.kt
package com.example.bigproj.presentation.Screen.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.bigproj.presentation.Screen.CreateSurveyScreen
import com.example.bigproj.presentation.Screen.DoctorsScreen
import com.example.bigproj.presentation.Screen.ManageSurveysScreen
import com.example.bigproj.presentation.Screen.SurveyListScreen
import com.example.bigproj.presentation.navigation.Screen

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
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
                        icon = {
                            Text(item.title.take(1)) // Просто первая буква
                        },
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
                onNavigateToMain = { /* ничего не делаем */ }
            )
            1 -> DoctorsScreen(
                navController = navController,
                onNavigateToCreateSurvey = {
                    navController.navigate("create_survey")
                },
                onNavigateToManageSurveys = {
                    navController.navigate("manage_surveys")
                }
            )
            2 -> MainScreen( // Старый мейн скрин в настройках
                onNavigateTo = onNavigateTo,
                navController = navController
            )
        }
    }
}