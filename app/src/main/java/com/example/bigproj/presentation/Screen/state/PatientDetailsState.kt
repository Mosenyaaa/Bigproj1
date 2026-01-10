// presentation/Screen/state/PatientDetailsState.kt
package com.example.bigproj.presentation.Screen.state

import com.example.bigproj.data.model.PatientAttemptDto
import com.example.bigproj.data.model.PatientDto
import com.example.bigproj.data.model.ScheduledSurveyDto

sealed class PatientDetailsEvent {
    data class LoadPatientHistory(val patientId: Int) : PatientDetailsEvent()
    data class LoadPatientAppointments(val patientId: Int) : PatientDetailsEvent()
}

enum class PatientDetailsTab {
    HISTORY,
    APPOINTMENTS
}

data class PatientDetailsState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val patient: PatientDto? = null,
    val selectedTab: PatientDetailsTab = PatientDetailsTab.HISTORY,
    val history: List<PatientAttemptDto> = emptyList(),
    val appointments: List<ScheduledSurveyDto> = emptyList(),
    val isLoadingHistory: Boolean = false,
    val isLoadingAppointments: Boolean = false
)
