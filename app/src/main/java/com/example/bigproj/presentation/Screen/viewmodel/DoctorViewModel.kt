// presentation/Screen/viewmodel/DoctorViewModel.kt (исправленная версия)
package com.example.bigproj.presentation.Screen.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.domain.repository.DoctorRepository
import com.example.bigproj.presentation.Screen.state.DoctorScreenEvent
import com.example.bigproj.presentation.Screen.state.DoctorScreenState
import com.example.bigproj.presentation.Screen.state.DoctorView
import kotlinx.coroutines.launch

class DoctorViewModel : ViewModel() {

    var state by mutableStateOf(DoctorScreenState())
        private set

    private lateinit var doctorRepository: DoctorRepository

    fun setupDependencies(context: Context) {
        doctorRepository = DoctorRepository(context)
    }

    fun onEvent(event: DoctorScreenEvent) {
        when (event) {
            is DoctorScreenEvent.LoadPatients -> loadPatients()
            is DoctorScreenEvent.PatientSelected -> selectPatient(event.patientId)
            is DoctorScreenEvent.LoadPatientAttempts -> loadPatientAttempts(event.patientId)
            DoctorScreenEvent.NavigateBack -> navigateBack()
        }
    }

    private fun loadPatients() {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🔄 Начинаем загрузку пациентов...")
                val patientsResponse = doctorRepository.getPatients()

                state = state.copy(
                    isLoading = false,
                    patients = patientsResponse.patients
                )
                println("✅ Успешно загружено пациентов: ${patientsResponse.patients.size}")

            } catch (e: Exception) {
                println("❌ Ошибка загрузки пациентов: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка загрузки пациентов: ${e.message}"
                )
            }
        }
    }

    private fun loadPatientAttempts(patientId: Int) {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🔄 Загружаем ответы пациента ID: $patientId")
                val attemptsResponse = doctorRepository.getPatientSurveyAttempts(patientId)

                state = state.copy(
                    isLoading = false,
                    patientAttempts = listOf(attemptsResponse),
                    currentView = com.example.bigproj.presentation.Screen.state.DoctorView.PATIENT_ATTEMPTS
                )
                println("✅ Успешно загружено попыток: ${attemptsResponse.attempts.size}")

            } catch (e: Exception) {
                println("❌ Ошибка загрузки ответов пациента: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка загрузки ответов: ${e.message}"
                )
            }
        }
    }

    private fun selectPatient(patientId: Int) {
        state = state.copy(selectedPatientId = patientId)
    }

    private fun navigateBack() {
        state = state.copy(
            currentView = DoctorView.PATIENTS_LIST,
            patientAttempts = emptyList()
        )
    }

    fun clearError() {
        state = state.copy(errorMessage = null)
    }
}