// presentation/Screen/viewmodel/AppointmentsViewModel.kt
package com.example.bigproj.presentation.Screen.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.data.model.PatientDto
import com.example.bigproj.data.model.ScheduledSurveyDto
import com.example.bigproj.data.model.SurveyManagementResponseDto
import com.example.bigproj.domain.repository.DoctorRepository
import com.example.bigproj.presentation.Screen.state.AppointmentsEvent
import com.example.bigproj.presentation.Screen.state.AppointmentsState
import com.example.bigproj.presentation.Screen.state.ScheduledSurveyWithPatient
import kotlinx.coroutines.launch

class AppointmentsViewModel : ViewModel() {

    var state by mutableStateOf(AppointmentsState())
        private set

    private lateinit var doctorRepository: DoctorRepository

    fun setupDependencies(context: Context) {
        doctorRepository = DoctorRepository(context)
    }

    fun onEvent(event: AppointmentsEvent) {
        when (event) {
            is AppointmentsEvent.LoadAppointments -> loadAppointments()
            is AppointmentsEvent.RefreshAppointments -> loadAppointments()
            is AppointmentsEvent.DeleteAppointment -> deleteAppointment(event.appointmentId)
            is AppointmentsEvent.ToggleAppointmentStatus -> toggleAppointmentStatus(event.appointmentId, event.isActive)
        }
    }

    private fun loadAppointments() {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🔄 Загружаем все назначения врача...")
                
                // Get all patients
                val patientsResponse = doctorRepository.getPatients()
                val patients = patientsResponse.patients
                println("✅ Получено пациентов: ${patients.size}")

                // Get appointments for all patients
                val allAppointments = mutableListOf<ScheduledSurveyWithPatient>()
                
                patients.forEach { patient ->
                    try {
                        val appointments = doctorRepository.getPatientScheduledSurveys(
                            patientId = patient.id,
                            activeOnly = false // Get all appointments, not just active
                        )
                        println("✅ Назначений для пациента ${patient.fullName}: ${appointments.size}")
                        
                        appointments.forEach { appointment ->
                            allAppointments.add(
                                ScheduledSurveyWithPatient(
                                    appointment = appointment,
                                    patient = patient
                                )
                            )
                        }
                    } catch (e: Exception) {
                        println("⚠️ Ошибка загрузки назначений для пациента ${patient.id}: ${e.message}")
                    }
                }

                // Sort by start date (newest first)
                val sortedAppointments = allAppointments.sortedByDescending { 
                    it.appointment.startDate ?: ""
                }

                val activeCount = sortedAppointments.count { 
                    it.appointment.isActive == true 
                }

                state = state.copy(
                    isLoading = false,
                    appointments = sortedAppointments,
                    activeAppointmentsCount = activeCount
                )
                println("✅ Всего назначений загружено: ${sortedAppointments.size}, активных: $activeCount")
            } catch (e: Exception) {
                println("❌ Ошибка загрузки назначений: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка загрузки назначений: ${e.message}"
                )
            }
        }
    }

    private fun deleteAppointment(appointmentId: Int) {
        // TODO: Implement delete endpoint if available in API
        // For now, just reload
        state = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                // API might not have delete endpoint for scheduled surveys
                // In that case, we can deactivate it instead
                loadAppointments()
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка удаления: ${e.message}"
                )
            }
        }
    }

    private fun toggleAppointmentStatus(appointmentId: Int, isActive: Boolean) {
        // TODO: Implement pause/resume endpoint if available
        // For now, just reload
        state = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                // API might need a separate endpoint for pausing/resuming
                // For now, reload to reflect changes
                loadAppointments()
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка изменения статуса: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        state = state.copy(errorMessage = null)
    }
}
