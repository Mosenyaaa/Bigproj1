// data/model/UserModels.kt
package com.example.bigproj.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User1ClientSO(
    @SerialName("id") val id: Int,
    @SerialName("email") val email: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("is_verified") val isVerified: Boolean,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("creation_dt") val creationDate: String? = null
)

@Serializable
data class AvailableDoctorSO(
    @SerialName("id") val id: Int,
    @SerialName("email") val email: String? = null,
    @SerialName("username") val username: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("is_verified") val isVerified: Boolean,
    @SerialName("is_active") val isActive: Boolean
)

@Serializable
data class AvailableDoctorsListResponse(
    @SerialName("doctors") val doctors: List<AvailableDoctorSO>,
    @SerialName("total_count") val totalCount: Int,
    @SerialName("returned_count") val returnedCount: Int
)

@Serializable
data class DoctorAssociationRequest(
    @SerialName("doctor_id") val doctorId: Int
)

@Serializable
data class DoctorAssociationResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String,
    @SerialName("association_id") val associationId: Int? = null
)