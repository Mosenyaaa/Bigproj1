package com.example.bigproj.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDto(
    @SerialName("id") val id: Int,
    @SerialName("long_id") val longId: String? = null,
    @SerialName("slug") val slug: String? = null,
    @SerialName("creation_dt") val creationDate: String,
    @SerialName("email") val email: String?,
    @SerialName("username") val username: String? = null,
    @SerialName("full_name") val fullName: String?,
    @SerialName("roles") val roles: List<String> = emptyList(),
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("is_verified") val isVerified: Boolean? = null,
    @SerialName("password") val password: String? = null,
    @SerialName("extra_data") val extraData: Map<String, String>? = null,
    @SerialName("allowed_roles") val allowedRoles: List<String> = emptyList(),
    @SerialName("roles_has_admin") val rolesHasAdmin: Boolean = false,
    @SerialName("roles_has_client") val rolesHasClient: Boolean = false,
    @SerialName("roles_has_doctor") val rolesHasDoctor: Boolean = false,
    @SerialName("email_prefix") val emailPrefix: String? = null,
    @SerialName("user_tokens_amount") val userTokensAmount: Int = 0,
    @SerialName("verification_codes_amount") val verificationCodesAmount: Int = 0,
    @SerialName("last_active_user_token_value") val lastActiveUserTokenValue: String? = null,
    @SerialName("patients_count") val patientsCount: Int = 0,
    @SerialName("doctors_count") val doctorsCount: Int = 0
) {
    // 🔥 ВЫЧИСЛЯЕМОЕ СВОЙСТВО ДЛЯ ОБРАТНОЙ СОВМЕСТИМОСТИ
    val isDoctor: Boolean
        get() = roles.contains("doctor") || rolesHasDoctor
}
