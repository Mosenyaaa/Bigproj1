// presentation/Screen/state/CreateAppointmentState.kt
package com.example.bigproj.presentation.Screen.state

import com.example.bigproj.data.model.PatientDto
import com.example.bigproj.data.model.SurveyManagementResponseDto

sealed class CreateAppointmentEvent {
    object LoadPatients : CreateAppointmentEvent()
    object LoadSurveys : CreateAppointmentEvent()
    data class PatientSelected(val patient: PatientDto) : CreateAppointmentEvent()
    data class SurveySelected(val survey: SurveyManagementResponseDto) : CreateAppointmentEvent()
    data class FrequencyChanged(val frequency: FrequencyType) : CreateAppointmentEvent()
    data class TimesPerDayChanged(val times: Int) : CreateAppointmentEvent()
    data class StartDateChanged(val date: String) : CreateAppointmentEvent()
    data class EndDateChanged(val date: String?) : CreateAppointmentEvent()
    object SaveAppointment : CreateAppointmentEvent()
}

enum class FrequencyType(val displayName: String, val apiValue: String) {
    DAILY("Ежедневно", "daily"),
    WEEKLY("Еженедельно", "weekly"),
    MONTHLY("Ежемесячно", "monthly"),
    ONCE("Однократно", "once")
}

data class CreateAppointmentState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val patients: List<PatientDto> = emptyList(),
    val surveys: List<SurveyManagementResponseDto> = emptyList(),
    val selectedPatient: PatientDto? = null,
    val selectedSurvey: SurveyManagementResponseDto? = null,
    val frequency: FrequencyType = FrequencyType.DAILY,
    val timesPerDay: Int = 1,
    val startDate: String = "",
    val endDate: String? = null,
    val isSuccess: Boolean = false
) {
    val canSave: Boolean
        get() = selectedPatient != null && 
                selectedSurvey != null && 
                startDate.isNotBlank() &&
                (frequency != FrequencyType.DAILY || timesPerDay > 0)
}
