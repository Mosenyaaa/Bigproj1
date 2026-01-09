package com.example.bigproj.presentation.Screen.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.domain.repository.DoctorRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ScheduleSurveyState(
    val patientId: Int = 0,
    val surveyId: String = "",
    val title: String = "",
    val startDate: String = LocalDate.now().format(DateTimeFormatter.ISO_DATE),
    val time: String = "10:00:00",
    val maxReminders: String = "3",
    val reminderIntervalMinutes: String = "60",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val scheduled: List<com.example.bigproj.data.model.ScheduledSurveyDto> = emptyList()
) {
    val canSend: Boolean
        get() = surveyId.isNotBlank() && startDate.isNotBlank() && time.isNotBlank()
}

class ScheduleSurveyViewModel : ViewModel() {
    var state by mutableStateOf(ScheduleSurveyState())
        private set

    private var repository: DoctorRepository? = null

    fun setup(context: Context, patientId: Int) {
        repository = DoctorRepository(context)
        state = state.copy(patientId = patientId)
    }

    fun updateSurveyId(value: String) { state = state.copy(surveyId = value) }
    fun updateTitle(value: String) { state = state.copy(title = value) }
    fun updateStartDate(value: String) { state = state.copy(startDate = value) }
    fun updateTime(value: String) { state = state.copy(time = value) }
    fun updateMaxReminders(value: String) { state = state.copy(maxReminders = value) }
    fun updateReminderInterval(value: String) { state = state.copy(reminderIntervalMinutes = value) }

    fun loadSchedules() {
        val repo = repository ?: return
        state = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val list = repo.getPatientScheduledSurveys(state.patientId)
                state = state.copy(isLoading = false, scheduled = list)
            } catch (e: Exception) {
                state = state.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun schedule() {
        val repo = repository ?: return
        val surveyIdInt = state.surveyId.toIntOrNull()
        if (surveyIdInt == null) {
            state = state.copy(errorMessage = "ID опроса должен быть числом")
            return
        }
        state = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val request = com.example.bigproj.data.model.ScheduleSurveyRequestDto(
                    surveyId = surveyIdInt,
                    patientId = state.patientId,
                    title = state.title.ifBlank { null },
                    startDate = state.startDate,
                    scheduledTimes = listOf(state.time),
                    timezone = "UTC",
                    frequencyType = "once",
                    maxReminders = state.maxReminders.toIntOrNull(),
                    reminderIntervalMinutes = state.reminderIntervalMinutes.toIntOrNull()
                )
                repo.scheduleSurvey(request)
                loadSchedules()
            } catch (e: Exception) {
                state = state.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        state = state.copy(errorMessage = null)
    }
}
