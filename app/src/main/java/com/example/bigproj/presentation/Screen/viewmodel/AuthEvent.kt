// presentation/Screen/viewmodel/AuthEvent.kt
package com.example.bigproj.presentation.Screen.viewmodel

sealed class AuthEvent {
    object NavigateToVerification : AuthEvent()
    object NavigateToRegistration : AuthEvent()
}