package com.example.bigproj.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EnableDoctorFeaturesResponseDto(
    @SerialName("id") val id: Int,
    @SerialName("email") val email: String,
    @SerialName("full_name") val fullName: String?,
    @SerialName("is_doctor") val isDoctor: Boolean? = null,  // ← Меняем на Boolean?
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("creation_dt") val creationDate: String
)