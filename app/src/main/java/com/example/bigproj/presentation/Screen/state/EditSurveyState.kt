// presentation/Screen/state/EditSurveyState.kt
package com.example.bigproj.presentation.Screen.state

import com.example.bigproj.data.api.QuestionInSurveyDto
import com.example.bigproj.data.model.SurveyManagementResponseDto

sealed class EditSurveyEvent {
    object LoadSurvey : EditSurveyEvent()
    data class UpdateTitle(val title: String) : EditSurveyEvent()
    data class UpdateDescription(val description: String) : EditSurveyEvent()
    data class ChangeStatus(val status: String) : EditSurveyEvent()
    object SaveSurvey : EditSurveyEvent()
    data class RemoveQuestion(val questionInSurveyId: Int) : EditSurveyEvent()
    data class SwapQuestions(val index1: Int, val index2: Int) : EditSurveyEvent()
    object ShowAddQuestionDialog : EditSurveyEvent()
    object HideAddQuestionDialog : EditSurveyEvent()
    data class AddQuestionToSurvey(val questionId: Int) : EditSurveyEvent()
}

data class EditSurveyState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val survey: SurveyManagementResponseDto? = null,
    val questions: List<QuestionInSurveyDto> = emptyList(),
    val title: String = "",
    val description: String = "",
    val status: String = "draft",
    val showAddQuestionDialog: Boolean = false,
    val isSuccess: Boolean = false
)
