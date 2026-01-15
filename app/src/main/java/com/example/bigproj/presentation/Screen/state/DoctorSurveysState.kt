// presentation/Screen/state/DoctorSurveysState.kt
package com.example.bigproj.presentation.Screen.state

import com.example.bigproj.data.model.SurveySimpleDto

sealed class DoctorSurveysEvent {
    object LoadSurveys : DoctorSurveysEvent()
    data class ChangeTab(val tab: DoctorSurveyTab) : DoctorSurveysEvent()
    data class SearchQueryChanged(val query: String) : DoctorSurveysEvent()
}

enum class DoctorSurveyTab(val displayName: String, val apiStatus: String?) {
    ALL("Все", null),
    DRAFTS("Черновики", "draft"),
    ACTIVE("Активные", "active"),
    ARCHIVE("Архив", "archived")
}

data class DoctorSurveysState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val surveys: List<SurveySimpleDto> = emptyList(),
    val selectedTab: DoctorSurveyTab = DoctorSurveyTab.ALL,
    val searchQuery: String = "",
    val totalCount: Int = 0,
    val returnedCount: Int = 0
)