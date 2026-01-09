// presentation/Screen/viewmodel/PatientDoctorsViewModel.kt
package com.example.bigproj.presentation.Screen.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bigproj.data.model.AvailableDoctorSO
import com.example.bigproj.data.model.User1ClientSO
import com.example.bigproj.domain.repository.DoctorRepository
import com.example.bigproj.presentation.Screen.state.*
import kotlinx.coroutines.launch

class PatientDoctorsViewModel : ViewModel() {

    var state by mutableStateOf(PatientDoctorsState())
        private set

    private lateinit var doctorRepository: DoctorRepository

    fun setupDependencies(context: Context) {
        doctorRepository = DoctorRepository(context)
    }

    fun onEvent(event: PatientDoctorsEvent) {
        when (event) {
            PatientDoctorsEvent.LoadMyDoctors -> loadMyDoctors()
            PatientDoctorsEvent.LoadAllDoctors -> loadAllDoctors()
            is PatientDoctorsEvent.SearchDoctors -> searchDoctors(event.query)
            is PatientDoctorsEvent.AssociateDoctor -> associateDoctor(event.doctorId)
            is PatientDoctorsEvent.DisassociateDoctor -> disassociateDoctor(event.doctorId)
            is PatientDoctorsEvent.SelectTab -> selectTab(event.tab)
            PatientDoctorsEvent.ShowAssociateDialog ->
                state = state.copy(showAssociateDialog = true)
            PatientDoctorsEvent.ShowDisassociateDialog ->
                state = state.copy(showDisassociateDialog = true)
            PatientDoctorsEvent.HideAssociateDialog ->
                state = state.copy(showAssociateDialog = false, selectedDoctor = null)
            PatientDoctorsEvent.HideDisassociateDialog ->
                state = state.copy(showDisassociateDialog = false, selectedDoctor = null)
            is PatientDoctorsEvent.SetSelectedDoctor ->
                state = state.copy(selectedDoctor = event.doctor)
        }
    }

    private fun loadMyDoctors() {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🔄 Загружаем моих врачей...")
                val myDoctorsResponse = doctorRepository.getMyDoctors()

                state = state.copy(
                    isLoading = false,
                    myDoctors = myDoctorsResponse,
                    selectedTab = PatientDoctorsTab.MY_DOCTORS
                )
                println("✅ Успешно загружено врачей: ${myDoctorsResponse.size}")

            } catch (e: Exception) {
                println("❌ Ошибка загрузки моих врачей: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка загрузки врачей: ${e.message}"
                )
            }
        }
    }

    private fun loadAllDoctors() {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🔄 Загружаем всех врачей...")
                val allDoctorsResponse = doctorRepository.getAvailableDoctors()

                // Преобразуем AvailableDoctorSO в User1ClientSO для совместимости
                val doctorsAsUser1ClientSO = allDoctorsResponse.doctors.map { availableDoctor ->
                    User1ClientSO(
                        id = availableDoctor.id,
                        email = availableDoctor.email,
                        fullName = availableDoctor.fullName,
                        username = availableDoctor.username,
                        isVerified = availableDoctor.isVerified,
                        isActive = availableDoctor.isActive,
                        creationDate = null
                    )
                }

                state = state.copy(
                    isLoading = false,
                    allDoctors = doctorsAsUser1ClientSO,
                    filteredDoctors = doctorsAsUser1ClientSO,
                    selectedTab = PatientDoctorsTab.ALL_DOCTORS
                )
                println("✅ Успешно загружено всех врачей: ${doctorsAsUser1ClientSO.size}")

            } catch (e: Exception) {
                println("❌ Ошибка загрузки всех врачей: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка загрузки врачей: ${e.message}"
                )
            }
        }
    }

    private fun searchDoctors(query: String) {
        state = state.copy(searchQuery = query)

        if (query.isBlank()) {
            state = state.copy(filteredDoctors = state.allDoctors)
        } else {
            val filtered = state.allDoctors.filter { doctor ->
                doctor.fullName?.contains(query, ignoreCase = true) == true ||
                        doctor.email?.contains(query, ignoreCase = true) == true ||
                        doctor.username?.contains(query, ignoreCase = true) == true
            }
            state = state.copy(filteredDoctors = filtered)
        }
        println("🔍 Поиск врачей: '$query' найдено ${state.filteredDoctors.size} врачей")
    }

    private fun associateDoctor(doctorId: Int) {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🔄 Привязываемся к врачу ID: $doctorId")
                val response = doctorRepository.associateDoctor(doctorId)

                if (response.success) {
                    // Перезагружаем списки
                    loadMyDoctors()
                    state = state.copy(
                        showAssociateDialog = false,
                        selectedDoctor = null,
                        errorMessage = "✅ Успешно привязались к врачу!"
                    )
                    println("✅ Успешно привязались к врачу ID: $doctorId")
                } else {
                    state = state.copy(
                        isLoading = false,
                        errorMessage = "Ошибка привязки: ${response.message}"
                    )
                }

            } catch (e: Exception) {
                println("❌ Ошибка привязки к врачу: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка привязки: ${e.message}"
                )
            }
        }
    }

    private fun disassociateDoctor(doctorId: Int) {
        state = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                println("🔄 Отвязываемся от врача ID: $doctorId")
                val response = doctorRepository.disassociateDoctor(doctorId)

                if (response.success) {
                    // Перезагружаем список моих врачей
                    loadMyDoctors()
                    state = state.copy(
                        showDisassociateDialog = false,
                        selectedDoctor = null,
                        errorMessage = "✅ Успешно отвязались от врача!"
                    )
                    println("✅ Успешно отвязались от врача ID: $doctorId")
                } else {
                    state = state.copy(
                        isLoading = false,
                        errorMessage = "Ошибка отвязки: ${response.message}"
                    )
                }

            } catch (e: Exception) {
                println("❌ Ошибка отвязки от врача: ${e.message}")
                state = state.copy(
                    isLoading = false,
                    errorMessage = "Ошибка отвязки: ${e.message}"
                )
            }
        }
    }

    private fun selectTab(tab: PatientDoctorsTab) {
        state = state.copy(selectedTab = tab)

        when (tab) {
            PatientDoctorsTab.MY_DOCTORS -> loadMyDoctors()
            PatientDoctorsTab.ALL_DOCTORS -> loadAllDoctors()
        }
    }

    fun clearError() {
        state = state.copy(errorMessage = null)
    }
}