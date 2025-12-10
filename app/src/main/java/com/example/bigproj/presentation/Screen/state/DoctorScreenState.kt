// presentation/Screen/state/DoctorScreenState.kt
package com.example.bigproj.presentation.Screen.state

import com.example.bigproj.data.model.PatientDto

sealed class DoctorScreenEvent {
    object LoadPatients : DoctorScreenEvent()
    data class PatientSelected(val patientId: Int) : DoctorScreenEvent()
    data class LoadPatientAttempts(val patientId: Int) : DoctorScreenEvent()
    object NavigateBack : DoctorScreenEvent()
}

data class DoctorScreenState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val patients: List<PatientDto> = emptyList(),
    val patientAttempts: List<com.example.bigproj.data.model.PatientAttemptsResponse> = emptyList(),
    val selectedPatientId: Int? = null,
    val currentView: DoctorView = DoctorView.PATIENTS_LIST
)

enum class DoctorView {
    PATIENTS_LIST,
    PATIENT_ATTEMPTS
}