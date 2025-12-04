// data/VerifyCodeResponseDto.kt
package com.example.bigproj.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VerifyCodeResponseDto(
    @SerialName("has_error") val hasError: Boolean? = null,
    @SerialName("error_code") val errorCode: String? = null,
    @SerialName("error_specification_code") val errorSpecificationCode: String? = null,
    @SerialName("error_description") val errorDescription: String? = null,
    @SerialName("error_description_data") val errorDescriptionData: Map<String, String>? = null,
    @SerialName("error_data") val errorData: Map<String, String>? = null,

    // 🔥 ОСНОВНЫЕ ДАННЫЕ ИЗ ComplicatedUserToken1GeneralSO
    @SerialName("id") val id: Int? = null,
    @SerialName("long_id") val longId: String? = null,
    @SerialName("slug") val slug: String? = null,
    @SerialName("creation_dt") val creationDate: String? = null,
    @SerialName("value") val value: String? = null,
    @SerialName("user_id") val userId: Int? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
    @SerialName("extra_data") val extraData: Map<String, String>? = null,

    // 🔥 ПОЛНЫЕ ДАННЫЕ ПОЛЬЗОВАТЕЛЯ
    @SerialName("user") val user: UserResponseDto? = null,

    @SerialName("is_ok") val isOk: Boolean? = null,
    val success: Boolean? = null
)
