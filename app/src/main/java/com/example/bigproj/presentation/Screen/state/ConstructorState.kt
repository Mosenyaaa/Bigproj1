// presentation/Screen/state/ConstructorState.kt
package com.example.bigproj.presentation.Screen.state

import com.example.bigproj.data.api.QuestionResponseDto

sealed class ConstructorEvent {
    object LoadQuestions : ConstructorEvent()
    data class SearchQuestions(val query: String) : ConstructorEvent()
    data class DeleteQuestion(val questionId: Int) : ConstructorEvent()
}

data class ConstructorState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val questions: List<QuestionResponseDto> = emptyList(),
    val searchQuery: String = "",
    val filteredQuestions: List<QuestionResponseDto> = emptyList()
) {
    val questionsCount: Int
        get() = filteredQuestions.size
}
