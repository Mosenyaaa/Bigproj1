package com.example.bigproj.presentation.Screen.state

sealed class MainScreenEvent {
    object LoadUserData : MainScreenEvent()
    data class UpdateFullName(val newName: String) : MainScreenEvent()
    data class EnableDoctorFeatures(val accessKey: String) : MainScreenEvent()
    object Logout : MainScreenEvent()
    object ShowEditDialog : MainScreenEvent()
    object HideEditDialog : MainScreenEvent()
    object ShowDoctorDialog : MainScreenEvent()
    object HideDoctorDialog : MainScreenEvent()
    object ShowEmailDialog : MainScreenEvent()
    object HideEmailDialog : MainScreenEvent()
    data class UpdateEmail(val newEmail: String) : MainScreenEvent()
    data class VerifyEmailCode(val code: String) : MainScreenEvent()

    // 🔥 ДОБАВЛЯЕМ СОБЫТИЯ ДЛЯ ДИАЛОГА ВЫХОДА
    object ShowLogoutDialog : MainScreenEvent()
    object HideLogoutDialog : MainScreenEvent()
    object ConfirmLogout : MainScreenEvent()
}

data class MainScreenState(
    val isLoading: Boolean = false,
    val userEmail: String = "",
    val userName: String = "",
    val isDoctor: Boolean = false,
    val errorMessage: String? = null,
    val showEditDialog: Boolean = false,
    val showDoctorDialog: Boolean = false,
    val showEmailDialog: Boolean = false,
    val showEmailVerificationDialog: Boolean = false,
    val tempNewEmail: String = "",

    // 🔥 ДОБАВЛЯЕМ СОСТОЯНИЕ ДЛЯ ДИАЛОГА ВЫХОДА
    val showLogoutDialog: Boolean = false
)