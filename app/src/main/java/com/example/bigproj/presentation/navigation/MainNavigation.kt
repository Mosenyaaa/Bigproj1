// presentation/navigation/MainNavigation.kt
package com.example.bigproj.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.bigproj.presentation.Screen.CreateSurveyScreen
import com.example.bigproj.presentation.Screen.EditSurveyScreen
import com.example.bigproj.presentation.Screen.LoginScreen
import com.example.bigproj.presentation.Screen.ManageSurveysScreen
import com.example.bigproj.presentation.Screen.QuestionEditorScreen
import com.example.bigproj.presentation.Screen.RegisterScreen
import com.example.bigproj.presentation.Screen.SurveyDetailScreen
import com.example.bigproj.presentation.Screen.VerificationScreen
import com.example.bigproj.presentation.Screen.main.MainScreenWithBottomNav
import kotlinx.serialization.Serializable

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
}

@Composable
fun MainNav(
    modifier: Modifier = Modifier,
    navHostController: NavHostController,
    startDestination: Screen
) {
    val context = LocalContext.current

    NavHost(
        modifier = modifier,
        navController = navHostController,
        startDestination = startDestination
    ) {
        composable<Screen.Login> {
            LoginScreen(
                onNavigateTo = { navigateTo ->
                    navHostController.navigate(navigateTo)
                }
            )
        }
        composable<Screen.Register> {
            RegisterScreen(
                onNavigateTo = { navigateTo ->
                    navHostController.navigate(navigateTo)
                }
            )
        }
        composable<Screen.Main> {
            MainScreenWithBottomNav(
                navController = navHostController,  // ✅ navController передается как параметр в composable
                onNavigateTo = { navigateTo ->
                    navHostController.navigate(navigateTo)  // ✅ navController доступен здесь
                }
            )
        }
        composable<Screen.Verification> { backStackEntry ->
            val email = backStackEntry.toRoute<Screen.Verification>().email

            VerificationScreen(
                onNavigateTo = { navigateTo ->
                    navHostController.navigate(navigateTo)
                },
                email = email,
                onDifferentEmail = {
                    navHostController.navigate(Screen.Login)
                },
                context = context
            )
        }
        composable<Screen.SurveyDetail> { backStackEntry ->
            val surveyId = backStackEntry.toRoute<Screen.SurveyDetail>().surveyId
            SurveyDetailScreen(
                surveyId = surveyId,
                onNavigateBack = { navHostController.popBackStack() },
                onSurveyCompleted = {
                    navHostController.navigate(Screen.SurveyList)
                }
            )
        }
        composable<Screen.CreateSurvey> {
            CreateSurveyScreen(
                onBackClick = { navHostController.popBackStack() },
                onSurveyCreated = { surveyId ->
                    // Можно показать сообщение об успехе или перейти куда-то
                    navHostController.popBackStack()
                }
            )
        }

        composable<Screen.QuestionEditor> { backStackEntry ->
            val questionIndex = backStackEntry.toRoute<Screen.QuestionEditor>().questionIndex
            QuestionEditorScreen(
                questionIndex = questionIndex,
                onBackClick = { navHostController.popBackStack() }
            )
        }
        composable<Screen.ManageSurveys> {
            ManageSurveysScreen(
                onBackClick = { navHostController.popBackStack() },
                onEditSurvey = { surveyId ->
                    navHostController.navigate(Screen.EditSurvey(surveyId))
                },
                onCreateSurvey = {
                    navHostController.navigate(Screen.CreateSurvey)
                }
            )
        }

        composable<Screen.EditSurvey> { backStackEntry ->
            val surveyId = backStackEntry.toRoute<Screen.EditSurvey>().surveyId
            EditSurveyScreen(
                surveyId = surveyId,
                onBackClick = { navHostController.popBackStack() },
                onSurveyUpdated = {
                    navHostController.popBackStack()
                }
            )
        }
    }
}