// presentation/Screen/viewmodel/DoctorSurveysViewModel.kt
package com.example.bigproj.presentation.Screen.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.domain.repository.DoctorRepository
import com.example.bigproj.presentation.Screen.state.DoctorSurveysEvent
import com.example.bigproj.presentation.Screen.state.DoctorSurveysState
import com.example.bigproj.presentation.Screen.state.DoctorSurveyTab
import kotlinx.coroutines.launch

class DoctorSurveysViewModel : ViewModel() {

    var state by mutableStateOf(DoctorSurveysState())
        private set

    private lateinit var doctorRepository: DoctorRepository

    fun setupDependencies(context: Context) {
        doctorRepository = DoctorRepository(context)
    }

    fun onEvent(event: DoctorSurveysEvent) {
        when (event) {
            is DoctorSurveysEvent.LoadSurveys -> loadSurveys()
            is DoctorSurveysEvent.ChangeTab -> {
                state = state.copy(selectedTab = event.tab)
                loadSurveys()
            }
            is DoctorSurveysEvent.SearchQueryChanged -> {
                state = state.copy(searchQuery = event.query)
                loadSurveys()
            }
        }
    }

    private fun loadSurveys() {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🔄 Загружаем опросы врача: tab=${state.selectedTab}, query='${state.searchQuery}'")
                
                val response = doctorRepository.getDoctorSurveys(
                    status = state.selectedTab.apiStatus,
                    query = state.searchQuery.takeIf { it.isNotBlank() },
                    start = 0,
                    finish = null,
                    limit = 20
                )

                state = state.copy(
                    isLoading = false,
                    surveys = response.surveys,
                    totalCount = response.totalCount,
                    returnedCount = response.returnedCount
                )
                println("✅ Загружено опросов: ${response.returnedCount} из ${response.totalCount}")
            } catch (e: Exception) {
                println("❌ Ошибка загрузки опросов: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка загрузки опросов: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        state = state.copy(errorMessage = null)
    }
}
