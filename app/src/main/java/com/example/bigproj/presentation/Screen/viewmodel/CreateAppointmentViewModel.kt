// presentation/Screen/viewmodel/CreateAppointmentViewModel.kt
package com.example.bigproj.presentation.Screen.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.data.model.PatientDto
import com.example.bigproj.data.model.ScheduleSurveyRequestDto
import com.example.bigproj.data.model.SurveyManagementResponseDto
import com.example.bigproj.domain.repository.DoctorRepository
import com.example.bigproj.presentation.Screen.state.CreateAppointmentEvent
import com.example.bigproj.presentation.Screen.state.CreateAppointmentState
import com.example.bigproj.presentation.Screen.state.FrequencyType
import kotlinx.coroutines.launch

class CreateAppointmentViewModel : ViewModel() {

    var state by mutableStateOf(CreateAppointmentState())
        private set

    private lateinit var doctorRepository: DoctorRepository

    fun setupDependencies(context: Context) {
        doctorRepository = DoctorRepository(context)
    }

    fun onEvent(event: CreateAppointmentEvent) {
        when (event) {
            is CreateAppointmentEvent.LoadPatients -> loadPatients()
            is CreateAppointmentEvent.LoadSurveys -> loadSurveys()
            is CreateAppointmentEvent.PatientSelected -> {
                state = state.copy(selectedPatient = event.patient)
            }
            is CreateAppointmentEvent.SurveySelected -> {
                state = state.copy(selectedSurvey = event.survey)
            }
            is CreateAppointmentEvent.FrequencyChanged -> {
                state = state.copy(
                    frequency = event.frequency,
                    timesPerDay = if (event.frequency == FrequencyType.DAILY) state.timesPerDay else 1
                )
            }
            is CreateAppointmentEvent.TimesPerDayChanged -> {
                state = state.copy(timesPerDay = event.times)
            }
            is CreateAppointmentEvent.StartDateChanged -> {
                state = state.copy(startDate = event.date)
            }
            is CreateAppointmentEvent.EndDateChanged -> {
                state = state.copy(endDate = event.date)
            }
            CreateAppointmentEvent.SaveAppointment -> saveAppointment()
        }
    }

    private fun loadPatients() {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🔄 Загружаем пациентов...")
                val patientsResponse = doctorRepository.getPatients()
                state = state.copy(
                    isLoading = false,
                    patients = patientsResponse.patients
                )
                println("✅ Загружено пациентов: ${patientsResponse.patients.size}")
            } catch (e: Exception) {
                println("❌ Ошибка загрузки пациентов: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка загрузки пациентов: ${e.message}"
                )
            }
        }
    }

    private fun loadSurveys() {
        if (state.surveys.isNotEmpty()) return // Already loaded

        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🔄 Загружаем опросы врача...")
                val surveysResponse = doctorRepository.getDoctorSurveys()
                // Filter only active surveys
                val activeSurveys = surveysResponse.surveys.filter { it.status == "active" }

                // КОНВЕРТИРУЕМ SurveySimpleDto в SurveyManagementResponseDto
                val convertedSurveys = activeSurveys.map { survey ->
                    SurveyManagementResponseDto(
                        id = survey.id,
                        title = survey.title,
                        description = survey.description,
                        status = survey.status,
                        userId = survey.userId,
                        isPublic = false, // По умолчанию
                        creationDate = survey.creationDate,
                        longId = null,
                        slug = null,
                        extraData = null
                    )
                }

                state = state.copy(
                    isLoading = false,
                    surveys = convertedSurveys
                )
                println("✅ Загружено активных опросов: ${convertedSurveys.size}")
            } catch (e: Exception) {
                println("❌ Ошибка загрузки опросов: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка загрузки опросов: ${e.message}"
                )
            }
        }
    }

    private fun saveAppointment() {
        val patient = state.selectedPatient ?: run {
            state = state.copy(errorMessage = "Выберите пациента")
            return
        }

        val survey = state.selectedSurvey ?: run {
            state = state.copy(errorMessage = "Выберите опрос")
            return
        }

        if (state.startDate.isBlank()) {
            state = state.copy(errorMessage = "Укажите дату начала")
            return
        }

        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val request = ScheduleSurveyRequestDto(
                    surveyId = survey.id,
                    patientId = patient.id,
                    title = survey.title,
                    description = survey.description,
                    frequencyType = state.frequency.apiValue,
                    timesPerDay = if (state.frequency == FrequencyType.DAILY) state.timesPerDay else null,
                    intervalDays = null, // TODO: Support interval days for custom frequency
                    daysOfWeek = null, // TODO: Support days of week for weekly
                    startDate = state.startDate,
                    endDate = state.endDate,
                    scheduledTimes = if (state.frequency == FrequencyType.DAILY) {
                        // Default time for daily surveys
                        listOf("10:00:00")
                    } else {
                        null
                    },
                    timezone = "UTC",
                    maxReminders = 3,
                    reminderIntervalMinutes = 60
                )

                println("📦 Создаем назначение: surveyId=${survey.id}, patientId=${patient.id}")
                val created = doctorRepository.scheduleSurvey(request)
                println("✅ Назначение создано: ${created.id}")

                state = state.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } catch (e: Exception) {
                println("❌ Ошибка создания назначения: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка создания назначения: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        state = state.copy(errorMessage = null)
    }

    fun resetSuccess() {
        state = state.copy(isSuccess = false)
    }
}
