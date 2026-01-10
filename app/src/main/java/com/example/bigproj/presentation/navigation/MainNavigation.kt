// presentation/navigation/MainNavigation.kt
package com.example.bigproj.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.bigproj.presentation.Screen.CreateAppointmentScreen
import com.example.bigproj.presentation.Screen.CreateSurveyScreen
import com.example.bigproj.presentation.Screen.DoctorsScreen
import com.example.bigproj.presentation.Screen.EditSurveyScreen
import com.example.bigproj.presentation.Screen.ManageSurveysScreen
import com.example.bigproj.presentation.Screen.QuestionEditorScreen
import com.example.bigproj.presentation.Screen.SurveyDetailScreen
import com.example.bigproj.presentation.Screen.SurveyListScreen
import com.example.bigproj.presentation.Screen.main.MainScreen
import com.example.bigproj.presentation.Screen.main.MainScreenWithBottomNav
import kotlinx.serialization.Serializable
import com.example.bigproj.presentation.Screen.PatientDoctorsScreen

sealed class Screen {
    @Serializable
    data object Login : Screen()

    @Serializable
    data object Register : Screen()

    @Serializable
    data object Main : Screen()

    @Serializable
    data class Verification(val email: String) : Screen()

    @Serializable
    data object SurveyList : Screen()

    @Serializable
    data class SurveyDetail(val surveyId: Int) : Screen()

    @Serializable
    data object CreateSurvey : Screen()

    @Serializable
    data class QuestionEditor(val questionIndex: Int) : Screen()

    @Serializable
    data object ManageSurveys : Screen()

    @Serializable
    data class EditSurvey(val surveyId: Int) : Screen()

    @Serializable
    data object PatientDoctors : Screen()
}

@Composable
fun MainNav(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    startDestination: Screen
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    NavHost(
        modifier = modifier,
        navController = navHostController,
        startDestination = startDestination
    ) {
        composable<Screen.Login> {
            com.example.bigproj.presentation.Screen.LoginScreen(
                onNavigateTo = { target ->
                    navHostController.navigate(target)
                }
            )
        }

        composable<Screen.Register> {
            com.example.bigproj.presentation.Screen.RegisterScreen(
                onNavigateTo = { target ->
                    navHostController.navigate(target)
                }
            )
        }

        composable<Screen.Main> {
            MainScreenWithBottomNav(
                navController = navHostController,
                onNavigateTo = { screen ->
                    when (screen) {
                        is Screen.SurveyDetail -> {
                            navHostController.navigate("survey_detail/${screen.surveyId}")
                        }
                        else -> {
                            // Обработка других экранов
                        }
                    }
                }
            )
        }

        composable<Screen.Verification> { backStackEntry ->
            val email = backStackEntry.toRoute<Screen.Verification>().email
            com.example.bigproj.presentation.Screen.VerificationScreen(
                email = email,
                onNavigateTo = { target ->
                    navHostController.navigate(target)
                },
                onDifferentEmail = {
                    navHostController.popBackStack(route = Screen.Login, inclusive = false)
                },
                context = context
            )
        }

        // ЭКРАНЫ ДЛЯ КОНСТРУКТОРА ОПРОСОВ
        composable("create_survey") {
            CreateSurveyScreen(
                onBackClick = { navHostController.popBackStack() },
                onSurveyCreated = { surveyId ->
                    println("✅ Опрос создан: $surveyId")
                    navHostController.popBackStack()
                },
                onEditQuestion = { questionIndex ->
                    navHostController.navigate("question_editor/$questionIndex")
                }
            )
        }

        composable("manage_surveys") {
            ManageSurveysScreen(
                onBackClick = { navHostController.popBackStack() },
                onEditSurvey = { surveyId ->
                    navHostController.navigate("edit_survey/$surveyId")
                },
                onCreateSurvey = {
                    navHostController.navigate("create_survey")
                }
            )
        }

        composable("edit_survey/{surveyId}") { backStackEntry ->
            val surveyId = backStackEntry.arguments?.getString("surveyId")?.toIntOrNull() ?: 0
            EditSurveyScreen(
                surveyId = surveyId,
                onBackClick = { navHostController.popBackStack() },
                onSurveyUpdated = { navHostController.popBackStack() },
                onEditQuestion = { questionIndex ->
                    navHostController.navigate("question_editor/$questionIndex")
                }
            )
        }

        composable("question_editor/{questionIndex}") { backStackEntry ->
            val questionIndex = backStackEntry.arguments?.getString("questionIndex")?.toIntOrNull() ?: 0
            QuestionEditorScreen(
                questionIndex = questionIndex,
                onBackClick = { navHostController.popBackStack() }
            )
        }

        // ЭКРАНЫ ДЛЯ ОПРОСОВ
        composable("survey_detail/{surveyId}") { backStackEntry ->
            val surveyId = backStackEntry.arguments?.getString("surveyId")?.toIntOrNull() ?: 0
            SurveyDetailScreen(
                surveyId = surveyId,
                onNavigateBack = { navHostController.popBackStack() },
                onSurveyCompleted = { navHostController.popBackStack() }
            )
        }

        // ЭКРАНЫ ДЛЯ ВРАЧЕЙ
        composable("doctors_screen") {
            DoctorsScreen(
                navController = navHostController,
                onNavigateToCreateSurvey = {
                    navHostController.navigate("create_survey")
                },
                onNavigateToManageSurveys = {
                    navHostController.navigate("manage_surveys")
                }
            )
        }

        composable("schedule_survey/{patientId}") { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")?.toIntOrNull() ?: 0
            com.example.bigproj.presentation.Screen.ScheduleSurveyScreen(
                patientId = patientId,
                onBack = { navHostController.popBackStack() }
            )
        }

        composable("patient_details/{patientId}") { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")?.toIntOrNull() ?: 0
            com.example.bigproj.presentation.Screen.PatientDetailsScreen(
                patientId = patientId,
                patient = null, // Will be loaded from state
                onBackClick = { navHostController.popBackStack() }
            )
        }

        // Constructor screens
        composable("create_question") {
            com.example.bigproj.presentation.Screen.CreateQuestionScreen(
                navController = navHostController
            )
        }

        composable("edit_question/{questionId}") { backStackEntry ->
            val questionId = backStackEntry.arguments?.getString("questionId")?.toIntOrNull() ?: 0
            com.example.bigproj.presentation.Screen.EditQuestionScreen(
                questionId = questionId,
                navController = navHostController
            )
        }

        // Appointments screens
        composable("create_appointment") {
            com.example.bigproj.presentation.Screen.CreateAppointmentScreen(
                navController = navHostController
            )
        }

        composable<Screen.PatientDoctors> {
            PatientDoctorsScreen()
        }

        composable<Screen.SurveyList> {
            SurveyListScreen(
                onNavigateToSurvey = { surveyId ->
                    navHostController.navigate("survey_detail/$surveyId")
                },
                onNavigateToMain = {
                    navHostController.navigate(Screen.Main)
                }
            )
        }

        composable("create_appointment") {
            CreateAppointmentScreen(
                navController = navHostController
            )
        }
    }
}