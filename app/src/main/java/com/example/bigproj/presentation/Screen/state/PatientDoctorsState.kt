// presentation/Screen/state/PatientDoctorsState.kt
package com.example.bigproj.presentation.Screen.state

import com.example.bigproj.data.model.User1ClientSO

sealed class PatientDoctorsEvent {
    object LoadMyDoctors : PatientDoctorsEvent()
    object LoadAllDoctors : PatientDoctorsEvent()
    data class SearchDoctors(val query: String) : PatientDoctorsEvent()
    data class AssociateDoctor(val doctorId: Int) : PatientDoctorsEvent()
    data class DisassociateDoctor(val doctorId: Int) : PatientDoctorsEvent()
    data class SelectTab(val tab: PatientDoctorsTab) : PatientDoctorsEvent()
    object ShowAssociateDialog : PatientDoctorsEvent()
    object ShowDisassociateDialog : PatientDoctorsEvent()
    object HideAssociateDialog : PatientDoctorsEvent()
    object HideDisassociateDialog : PatientDoctorsEvent()
    data class SetSelectedDoctor(val doctor: User1ClientSO?) : PatientDoctorsEvent()
}

data class PatientDoctorsState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val myDoctors: List<User1ClientSO> = emptyList(),
    val allDoctors: List<User1ClientSO> = emptyList(),
    val filteredDoctors: List<User1ClientSO> = emptyList(),
    val searchQuery: String = "",
    val selectedTab: PatientDoctorsTab = PatientDoctorsTab.MY_DOCTORS,
    val showAssociateDialog: Boolean = false,
    val showDisassociateDialog: Boolean = false,
    val selectedDoctor: User1ClientSO? = null
)

enum class PatientDoctorsTab {
    MY_DOCTORS,
    ALL_DOCTORS
}