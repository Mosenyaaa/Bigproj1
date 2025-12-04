// presentation/Screen/state/SurveyScreenState.kt
package com.example.bigproj.presentation.Screen.state

sealed class SurveyScreenEvent {
    object LoadSurveys : SurveyScreenEvent()
    data class ChangeTab(val tab: SurveyTab) : SurveyScreenEvent()
    data class SearchQueryChanged(val query: String) : SurveyScreenEvent()
    data class StartSurvey(val surveyId: Int) : SurveyScreenEvent()
    data class SelectSymptom(val symptom: String) : SurveyScreenEvent()
    data class SetWellBeingRating(val rating: Int) : SurveyScreenEvent()
    data class AnswerQuestion(val questionId: Int, val answer: String) : SurveyScreenEvent()
    object NavigateToNextStep : SurveyScreenEvent()
    object NavigateToPreviousStep : SurveyScreenEvent()
    object SubmitSurvey : SurveyScreenEvent()
}

enum class SurveyTab {
    NEW, STARTED, COMPLETED
}

data class SurveyScreenState(
    val isLoading: Boolean = false,
    val surveys: List<SurveyUiModel> = emptyList(),
    val selectedTab: SurveyTab = SurveyTab.NEW,
    val searchQuery: String = "",
    val errorMessage: String? = null,

    // Текущий опрос
    val currentSurvey: SurveyUiModel? = null,
    val currentStep: Int = 0,
    val selectedSymptoms: Set<String> = emptySet(),
    val wellBeingRating: Int = 0,
    val answers: Map<Int, String> = emptyMap()
)

data class SurveyUiModel(
    val id: Int,
    val title: String,
    val description: String?,
    val status: String,
    val progress: Float = 0f // 0.0 - 1.0
)