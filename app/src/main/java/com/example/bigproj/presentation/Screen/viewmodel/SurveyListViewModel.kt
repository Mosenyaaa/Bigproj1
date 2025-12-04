// presentation/Screen/viewmodel/SurveyListViewModel.kt
package com.example.bigproj.presentation.Screen.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.domain.repository.SurveyRepository
import com.example.bigproj.presentation.Screen.state.SurveyScreenEvent
import com.example.bigproj.presentation.Screen.state.SurveyScreenState
import com.example.bigproj.presentation.Screen.state.SurveyTab
import com.example.bigproj.presentation.Screen.state.SurveyUiModel
import kotlinx.coroutines.launch

class SurveyListViewModel : ViewModel() {
    var state by mutableStateOf(SurveyScreenState())
        private set

    private lateinit var surveyRepository: SurveyRepository

    fun setupDependencies(context: Context) {
        surveyRepository = SurveyRepository(context)
    }

    fun onEvent(event: SurveyScreenEvent) {
        when (event) {
            is SurveyScreenEvent.LoadSurveys -> loadSurveys()
            is SurveyScreenEvent.ChangeTab -> changeTab(event.tab)
            is SurveyScreenEvent.SearchQueryChanged -> searchQueryChanged(event.query)
            else -> {}
        }
    }

    private fun loadSurveys() {
        println("📋 Начинаем загрузку опросов через комбинированный метод")
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                // 🔥 ИСПОЛЬЗУЕМ КОМБИНИРОВАННЫЙ МЕТОД (ТОЛЬКО РЕАЛЬНЫЕ ОПРОСЫ)
                val surveys = surveyRepository.getSurveysCombined()

                println("✅ Получено реальных опросов: ${surveys.size}")

                // 🔥 ЗАГРУЖАЕМ ПРОГРЕСС ДЛЯ КАЖДОГО РЕАЛЬНОГО ОПРОСА
                val surveysWithProgress = surveys.map { survey ->
                    try {
                        val progress = surveyRepository.getSurveyProgress(survey.id)

                        SurveyUiModel(
                            id = survey.id,
                            title = survey.title,
                            description = survey.description,
                            status = surveyRepository.determineStatus(progress),
                            progress = surveyRepository.calculateProgress(progress)
                        ).also {
                            println("📊 Реальный опрос ${survey.id}: ${it.status}, прогресс: ${it.progress}, вопросов: ${survey.questions.size}")
                        }
                    } catch (e: Exception) {
                        println("⚠️ Не удалось загрузить прогресс для реального опроса ${survey.id}: ${e.message}")
                        SurveyUiModel(
                            id = survey.id,
                            title = survey.title,
                            description = survey.description,
                            status = "new",
                            progress = 0f
                        )
                    }
                }

                // 🔥 ФИЛЬТРАЦИЯ ПО ВКЛАДКАМ
                val filteredSurveys = filterSurveysByTab(surveysWithProgress, state.selectedTab)

                state = state.copy(
                    isLoading = false,
                    surveys = filteredSurveys
                )
                println("✅ Отфильтровано реальных опросов: ${filteredSurveys.size} для вкладки ${state.selectedTab}")

            } catch (e: Exception) {
                println("❌ Ошибка загрузки реальных опросов: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка загрузки опросов: ${e.message}"
                )
            }
        }
    }

    // 🔥 ОБНОВЛЯЕМ ФИЛЬТРАЦИЮ
    private fun filterSurveysByTab(surveys: List<SurveyUiModel>, tab: SurveyTab): List<SurveyUiModel> {
        return when (tab) {
            SurveyTab.NEW -> surveys.filter { it.status == "new" }
            SurveyTab.STARTED -> surveys.filter { it.status == "started" }
            SurveyTab.COMPLETED -> surveys.filter { it.status == "completed" }
        }.also { filtered ->
            println("🔍 Фильтрация реальных опросов: ${surveys.size} -> ${filtered.size} для $tab")
        }
    }

    private fun changeTab(tab: SurveyTab) {
        state = state.copy(selectedTab = tab)
        println("🔁 Смена вкладки на: $tab")
        loadSurveys() // Перезагружаем с фильтрацией
    }

    private fun searchQueryChanged(query: String) {
        state = state.copy(searchQuery = query)

        // 🔥 ПЕРЕЗАГРУЖАЕМ С ФИЛЬТРАЦИЕЙ ПО ПОИСКУ
        viewModelScope.launch {
            try {
                val allSurveys = surveyRepository.getSurveysCombined()
                val surveysWithProgress = allSurveys.map { survey ->
                    try {
                        val progress = surveyRepository.getSurveyProgress(survey.id)
                        SurveyUiModel(
                            id = survey.id,
                            title = survey.title,
                            description = survey.description,
                            status = surveyRepository.determineStatus(progress),
                            progress = surveyRepository.calculateProgress(progress)
                        )
                    } catch (e: Exception) {
                        println("⚠️ Не удалось загрузить прогресс для реального опроса ${survey.id}: ${e.message}")
                        SurveyUiModel(
                            id = survey.id,
                            title = survey.title,
                            description = survey.description,
                            status = "new",
                            progress = 0f
                        )
                    }
                }

                // 🔥 ФИЛЬТР ПО ПОИСКУ + ПО ВКЛАДКЕ
                var filtered = surveysWithProgress
                if (query.isNotBlank()) {
                    filtered = filtered.filter {
                        it.title.contains(query, ignoreCase = true) ||
                                it.description?.contains(query, ignoreCase = true) == true
                    }
                }
                filtered = filterSurveysByTab(filtered, state.selectedTab)

                state = state.copy(surveys = filtered)
                println("🔍 Поиск '$query' в реальных опросах: найдено ${filtered.size} опросов")

            } catch (e: Exception) {
                println("❌ Ошибка поиска в реальных опросах: ${e.message}")
                state = state.copy(
                    errorMessage = "Ошибка поиска: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        state = state.copy(errorMessage = null)
    }
}