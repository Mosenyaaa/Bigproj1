// presentation/Screen/state/AppointmentsState.kt
package com.example.bigproj.presentation.Screen.state

import com.example.bigproj.data.model.PatientDto
import com.example.bigproj.data.model.ScheduledSurveyDto
import com.example.bigproj.data.model.SurveyManagementResponseDto

sealed class AppointmentsEvent {
    object LoadAppointments : AppointmentsEvent()
    object RefreshAppointments : AppointmentsEvent()
    data class DeleteAppointment(val appointmentId: Int) : AppointmentsEvent()
    data class ToggleAppointmentStatus(val appointmentId: Int, val isActive: Boolean) : AppointmentsEvent()
}

data class AppointmentsState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val appointments: List<ScheduledSurveyWithPatient> = emptyList(),
    val activeAppointmentsCount: Int = 0
)

// Extended model to include patient info
data class ScheduledSurveyWithPatient(
    val appointment: ScheduledSurveyDto,
    val patient: PatientDto? = null,
    val survey: SurveyManagementResponseDto? = null
)
