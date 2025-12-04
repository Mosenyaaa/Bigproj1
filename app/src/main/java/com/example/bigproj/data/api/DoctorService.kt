// data/api/DoctorService.kt
package com.example.bigproj.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface DoctorService {

    @GET("/api/doctor/my_patients")
    suspend fun getMyPatients(): Response<com.example.bigproj.data.model.PatientsListResponse>

    // 🔥 ТАКЖЕ МЕНЯЕМ ЭНДПОИНТ ДЛЯ ПОЛУЧЕНИЯ ОТВЕТОВ ПАЦИЕНТА
    @GET("/api/doctor/patient_attempts")
    suspend fun getPatientAttempts(
        @Query("patient_id") patientId: Int
    ): Response<com.example.bigproj.data.model.PatientAttemptsResponse>

    @GET("/api/doctor/my_surveys")
    suspend fun getMySurveys(): Response<com.example.bigproj.data.model.SurveyListResponseDto>
}