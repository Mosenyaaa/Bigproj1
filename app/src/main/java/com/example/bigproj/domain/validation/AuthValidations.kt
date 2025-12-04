// domain/validation/AuthValidations.kt
package com.example.bigproj.domain.validation

import android.util.Patterns

object AuthValidations {

    // 🔥 ВАЛИДАЦИЯ EMAIL ДЛЯ ЛОГИНА И РЕГИСТРАЦИИ
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Почта не может быть пустой")
            !email.contains("@") -> ValidationResult.Error("Почта должна содержать @")
            !email.contains(".") -> ValidationResult.Error("Почта должна содержать точку")
            email.length < 5 -> ValidationResult.Error("Почта слишком короткая")
            email.length > 100 -> ValidationResult.Error("Почта слишком длинная")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> ValidationResult.Error("Неверный формат почты")
            else -> ValidationResult.Success
        }
    }

    // 🔥 ВАЛИДАЦИЯ ИМЕНИ ДЛЯ РЕГИСТРАЦИИ
    fun validateName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Error("Имя не может быть пустым")
            name.length < 2 -> ValidationResult.Error("Имя должно содержать минимум 2 символа")
            name.length > 50 -> ValidationResult.Error("Имя слишком длинное")
            !name.matches(Regex("^[a-zA-Zа-яА-ЯёЁ\\s-]+$")) -> ValidationResult.Error("Имя может содержать только буквы, пробелы и дефисы")
            // 🔥 УБИРАЕМ ПРОВЕРКУ НА ИМЯ И ФАМИЛИЮ - ДЕЛАЕМ НЕОБЯЗАТЕЛЬНОЙ
            else -> ValidationResult.Success
        }
    }

    // 🔥 ВАЛИДАЦИЯ КОДА ПОДТВЕРЖДЕНИЯ
    fun validateVerificationCode(code: String): ValidationResult {
        return when {
            code.isBlank() -> ValidationResult.Error("Введите код подтверждения")
            code.length != 6 -> ValidationResult.Error("Код должен содержать 6 символов")
            !code.matches(Regex("^[a-zA-Z0-9]+$")) -> ValidationResult.Error("Код может содержать только буквы и цифры")
            else -> ValidationResult.Success
        }
    }

    // 🔥 ВАЛИДАЦИЯ СОГЛАСИЯ С УСЛОВИЯМИ
    fun validateTermsAgreement(agreed: Boolean): ValidationResult {
        return if (!agreed) {
            ValidationResult.Error("Необходимо согласие с условиями")
        } else {
            ValidationResult.Success
        }
    }

    // 🔥 ВАЛИДАЦИЯ ACCESS KEY ДЛЯ ВРАЧА
    fun validateAccessKey(accessKey: String): ValidationResult {
        return when {
            accessKey.isBlank() -> ValidationResult.Error("Введите access key")
            accessKey.length < 1 -> ValidationResult.Error("Access key слишком короткий")
            accessKey.length > 20 -> ValidationResult.Error("Access key слишком длинный")
            else -> ValidationResult.Success
        }
    }
}