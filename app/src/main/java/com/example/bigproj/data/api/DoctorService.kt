// data/api/DoctorService.kt
package com.example.bigproj.data.api

import com.example.bigproj.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface DoctorService {

    @GET("/api/doctor/my_patients")
    suspend fun getMyPatients(): Response<PatientsListResponse>

    @GET("/api/doctor/patient_attempts")
    suspend fun getPatientAttempts(
        @Query("patient_id") patientId: Int
    ): Response<PatientAttemptsResponse>

    @GET("/api/doctor/my_surveys")
    suspend fun getMySurveys(): Response<SurveyListResponseDto>

    @GET("/api/doctor/patient_scheduled_surveys")
    suspend fun getPatientScheduledSurveys(
        @Query("patient_id") patientId: Int
    ): Response<PatientScheduledSurveysResponse>

    @POST("/api/doctor/schedule_survey")
    suspend fun scheduleSurvey(
        @Body request: ScheduleSurveyRequestDto
    ): Response<ScheduledSurveyDto>

    // 🔥 НОВЫЕ МЕТОДЫ ДЛЯ РАБОТЫ С ВРАЧАМИ (пациентская часть)
    @GET("/api/client/my_doctors")
    suspend fun getMyDoctors(): Response<List<User1ClientSO>>

    @GET("/api/client/available_doctors")
    suspend fun getAvailableDoctors(
        @Query("query") query: String? = null,
        @Query("st") st: Int = 0,
        @Query("fn") fn: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<AvailableDoctorsListResponse>

    @POST("/api/client/doctor_association")
    suspend fun associateDoctor(
        @Body request: DoctorAssociationRequest
    ): Response<DoctorAssociationResponse>

    @HTTP(method = "DELETE", path = "/api/client/doctor_disassociation", hasBody = true)
    suspend fun disassociateDoctor(
        @Body request: DoctorAssociationRequest
    ): Response<DoctorAssociationResponse>
}