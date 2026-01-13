// presentation/Screen/state/CreateSurveyState.kt
package com.example.bigproj.presentation.Screen.state

sealed class CreateSurveyEvent {
    data class TitleChanged(val title: String) : CreateSurveyEvent()
    data class DescriptionChanged(val description: String) : CreateSurveyEvent()
    object CreateSurvey : CreateSurveyEvent()
}

data class CreateSurveyState(
    val title: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val createdSurveyId: Int? = null,
    val isSuccess: Boolean = false
) {
    val canCreate: Boolean
        get() = title.isNotBlank() && !isLoading
}
