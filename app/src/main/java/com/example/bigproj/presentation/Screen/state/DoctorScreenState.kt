// presentation/Screen/state/DoctorScreenState.kt
package com.example.bigproj.presentation.Screen.state

sealed class DoctorScreenEvent {
    object LoadPatients : DoctorScreenEvent()
    data class PatientSelected(val patientId: Int) : DoctorScreenEvent()
    data class LoadPatientAttempts(val patientId: Int) : DoctorScreenEvent()
    object NavigateBack : DoctorScreenEvent()
}

data class DoctorScreenState(
    val isLoading: Boolean = false,
    val patients: List<com.example.bigproj.data.model.PatientDto> = emptyList(),
    val errorMessage: String? = null,
    val selectedPatientId: Int? = null,
    val patientAttempts: List<com.example.bigproj.data.model.PatientAttemptDto> = emptyList(),
    val currentView: DoctorView = DoctorView.PATIENTS_LIST
)

enum class DoctorView {
    PATIENTS_LIST,
    PATIENT_ATTEMPTS
}