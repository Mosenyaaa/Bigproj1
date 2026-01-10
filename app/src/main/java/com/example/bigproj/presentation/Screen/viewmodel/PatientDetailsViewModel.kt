// presentation/Screen/viewmodel/PatientDetailsViewModel.kt
package com.example.bigproj.presentation.Screen.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.domain.repository.DoctorRepository
import com.example.bigproj.presentation.Screen.state.PatientDetailsEvent
import com.example.bigproj.presentation.Screen.state.PatientDetailsState
import com.example.bigproj.presentation.Screen.state.PatientDetailsTab
import kotlinx.coroutines.launch

class PatientDetailsViewModel : ViewModel() {

    var state by mutableStateOf(PatientDetailsState())
        private set

    private lateinit var doctorRepository: DoctorRepository

    fun setupDependencies(context: Context) {
        doctorRepository = DoctorRepository(context)
    }

    fun onEvent(event: PatientDetailsEvent) {
        when (event) {
            is PatientDetailsEvent.LoadPatientHistory -> loadPatientHistory(event.patientId)
            is PatientDetailsEvent.LoadPatientAppointments -> loadPatientAppointments(event.patientId)
        }
    }

    fun selectTab(tab: PatientDetailsTab) {
        state = state.copy(selectedTab = tab)
    }

    private fun loadPatientHistory(patientId: Int) {
        state = state.copy(isLoadingHistory = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🔄 Загружаем историю пациента ID: $patientId")
                val attemptsResponse = doctorRepository.getPatientSurveyAttempts(patientId)

                // Convert PatientInfoDto to PatientDto if available
                val patientDto = attemptsResponse.patientInfo?.let { info ->
                    com.example.bigproj.data.model.PatientDto(
                        id = info.id,
                        email = info.email,
                        username = info.username,
                        fullName = info.fullName,
                        isVerified = false, // Not available in PatientInfoDto
                        isActive = true, // Assume active
                        creationDate = info.creationDate
                    )
                }

                state = state.copy(
                    isLoadingHistory = false,
                    history = attemptsResponse.attempts,
                    patient = patientDto ?: state.patient
                )
                println("✅ Успешно загружено попыток: ${attemptsResponse.attempts.size}")

            } catch (e: Exception) {
                println("❌ Ошибка загрузки истории пациента: ${e.message}")
                state = state.copy(
                    isLoadingHistory = false,
                    errorMessage = "Ошибка загрузки истории: ${e.message}"
                )
            }
        }
    }

    private fun loadPatientAppointments(patientId: Int) {
        state = state.copy(isLoadingAppointments = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🔄 Загружаем назначения пациента ID: $patientId")
                val appointments = doctorRepository.getPatientScheduledSurveys(patientId)

                state = state.copy(
                    isLoadingAppointments = false,
                    appointments = appointments
                )
                println("✅ Успешно загружено назначений: ${appointments.size}")

            } catch (e: Exception) {
                println("❌ Ошибка загрузки назначений пациента: ${e.message}")
                state = state.copy(
                    isLoadingAppointments = false,
                    errorMessage = "Ошибка загрузки назначений: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        state = state.copy(errorMessage = null)
    }
}
