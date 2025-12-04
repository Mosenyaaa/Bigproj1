// presentation/Screen/state/LoginScreenState.kt
package com.example.bigproj.presentation.Screen.state

import com.example.bigproj.presentation.navigation.Screen

sealed class LoginScreenEvent {
    data class NavigateToScreen(val screen: Screen): LoginScreenEvent()
    data class EmailUpdated(val newEmail: String): LoginScreenEvent()
}

data class LoginScreenState(
    val email: String = "",
    val errorMessage: String? = null
)