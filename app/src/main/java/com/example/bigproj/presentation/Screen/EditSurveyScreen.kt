// presentation/Screen/EditSurveyScreen.kt
package com.example.bigproj.presentation.Screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bigproj.presentation.Screen.viewmodel.SurveyManagementViewModel

@Composable
fun EditSurveyScreen(
    surveyId: Int,
    onBackClick: () -> Unit = {},
    onSurveyUpdated: () -> Unit = {},
    onEditQuestion: (Int) -> Unit = {},
    externalViewModel: SurveyManagementViewModel? = null
) {
    val context = LocalContext.current
    // Позволяем передать общий ViewModel извне, чтобы делиться состоянием между экранами
    val viewModel = externalViewModel ?: viewModel<SurveyManagementViewModel>()

    // Загружаем существующий опрос
    LaunchedEffect(surveyId) {
        viewModel.setupDependencies(context)
        viewModel.loadExistingSurvey(surveyId)
    }

    // Используем тот же экран создания, но в режиме редактирования
    CreateSurveyScreen(
        onBackClick = onBackClick,
        onSurveyCreated = {
            // В режиме редактирования просто возвращаемся назад
            onSurveyUpdated()
        },
        onEditQuestion = onEditQuestion,
        externalViewModel = viewModel
    )
}