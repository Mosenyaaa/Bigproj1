// domain/repository/DoctorRepository.kt
package com.example.bigproj.domain.repository

import android.content.Context
import com.example.bigproj.data.RetrofitClient
import com.example.bigproj.data.model.*
import com.example.bigproj.domain.utils.ErrorHandler

class DoctorRepository(private val context: Context) {

    private val tokenManager = TokenManager(context)
    private val doctorService by lazy {
        RetrofitClient.createDoctorService(tokenManager)
    }

    suspend fun getPatients(): PatientsListResponse {
        println("👥 ДИАГНОСТИКА: Загружаем список пациентов через /api/doctor/my_patients")

        try {
            val response = doctorService.getMyPatients()

            println("📡 Ответ my_patients: код=${response.code()}, успешно=${response.isSuccessful}")
            println("📊 Полный ответ пациентов: ${response.body()}")

            if (response.isSuccessful) {
                val patientsResponse = response.body()
                println("✅ Пациенты загружены: ${patientsResponse?.patients?.size ?: 0}")

                val targetPatient = patientsResponse?.patients?.find { it.id == 6 }
                println("🎯 Поиск пациента ID=6: ${if (targetPatient != null) "НАЙДЕН" else "НЕ НАЙДЕН"}")
                println("🎯 Данные пациента ID=6: $targetPatient")

                patientsResponse?.patients?.forEachIndexed { index: Int, patient ->
                    println("👤 Пациент ${index + 1}:")
                    println("   ID: ${patient.id}")
                    println("   Имя: ${patient.fullName ?: "Не указано"}")
                    println("   Email: ${patient.email}")
                    println("   Верифицирован: ${patient.isVerified}")
                    println("   Активен: ${patient.isActive}")
                    println("   Дата создания: ${patient.creationDate}")
                }

                if (patientsResponse?.patients.isNullOrEmpty()) {
                    println("⚠️ Список пациентов ПУСТОЙ! Возможные причины:")
                    println("   - Пользователь не является врачом")
                    println("   - Врачу не назначены пациенты")
                    println("   - Пациенты не подтвердили регистрацию")
                    println("   - Ошибка на сервере с назначениями")
                }

                return patientsResponse ?: throw Exception("Пустой ответ от сервера")
            } else {
                val errorBody = response.errorBody()?.string()
                println("❌ Тело ошибки: $errorBody")
                val errorMessage = ErrorHandler.parseError(response)

                when (response.code()) {
                    401 -> println("🔐 Ошибка 401: Неавторизован - проверьте токен")
                    403 -> println("🔐 Ошибка 403: Доступ запрещен - пользователь не врач")
                    404 -> println("🔍 Ошибка 404: Endpoint не найден")
                    500 -> println("⚙️ Ошибка 500: Внутренняя ошибка сервера")
                    else -> println("❓ Другая ошибка: ${response.code()}")
                }

                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            println("❌ Ошибка загрузки пациентов: ${e.message}")
            throw e
        }
    }

    suspend fun getPatientSurveyAttempts(patientId: Int): PatientAttemptsResponse {
        println("📊 ДИАГНОСТИКА: Загружаем ответы пациента ID: $patientId")

        try {
            val response = doctorService.getPatientAttempts(patientId)

            println("📡 Ответ patient_attempts: код=${response.code()}, успешно=${response.isSuccessful}")
            println("📡 Полный ответ: ${response.body()}")

            if (response.isSuccessful) {
                val attempts = response.body()

                println("✅ Ответы пациента загружены:")
                println("   - attempts: ${attempts?.attempts?.size ?: 0}")
                println("   - patientInfo: ${attempts?.patientInfo}")
                println("   - totalCount: ${attempts?.totalCount}")
                println("   - returnedCount: ${attempts?.returnedCount}")

                if (attempts != null) {
                    println("🔍 Структура ответа:")
                    println("   - attempts class: ${attempts.attempts.javaClass.simpleName}")
                }

                attempts?.attempts?.forEachIndexed { index: Int, attempt ->
                    println("📝 Попытка ${index + 1}:")
                    println("   ID попытки: ${attempt.attemptId}")
                    println("   Опрос: ${attempt.surveyTitle}")
                    println("   Статус: ${attempt.status}")
                    println("   Дата: ${attempt.creationDate}")
                    println("   Ответов: ${attempt.answers.size}")

                    attempt.answers.forEachIndexed { ansIndex: Int, answer ->
                        println("   Ответ ${ansIndex + 1}:")
                        println("      Вопрос: ${answer.questionText}")
                        println("      Текст: ${answer.text ?: "нет"}")
                        println("      Голос: ${answer.voiceFilename ?: "нет"}")
                        println("      Фото: ${answer.pictureFilename ?: "нет"}")
                    }
                }

                if (attempts?.attempts.isNullOrEmpty()) {
                    println("⚠️ У пациента нет пройденных опросов")
                }

                return attempts ?: throw Exception("Пустой ответ от сервера")
            } else {
                val errorBody = response.errorBody()?.string()
                println("❌ Тело ошибки: $errorBody")
                val errorMessage = ErrorHandler.parseError(response)
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            println("❌ Ошибка загрузки ответов пациента: ${e.message}")
            throw e
        }
    }

    suspend fun getPatientScheduledSurveys(patientId: Int, activeOnly: Boolean = true): List<ScheduledSurveyDto> {
        val response = doctorService.getPatientScheduledSurveys(patientId, activeOnly)
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    suspend fun scheduleSurvey(request: ScheduleSurveyRequestDto): ScheduledSurveyDto {
        val response = doctorService.scheduleSurvey(request)
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Пустой ответ от сервера")
        } else {
            val errorMessage = ErrorHandler.parseError(response)
            throw Exception(errorMessage)
        }
    }

    suspend fun getDoctorSurveys(
        status: String? = null,
        query: String? = null,
        start: Int = 0,
        finish: Int? = null,
        limit: Int? = null
    ): SurveyListResponseDto {
        println("📋 Получаем опросы текущего врача: status=$status, query=$query, st=$start, limit=$limit")

        try {
            val response = doctorService.getMySurveys(
                status = status,
                query = query,
                st = start,
                fn = finish,
                limit = limit
            )

            println("📡 Ответ my_surveys: код=${response.code()}, успешно=${response.isSuccessful}")
            println("📡 Тело: ${response.body()}")

            if (response.isSuccessful) {
                val surveys = response.body()
                println("✅ Опросы врача: ${surveys?.surveys?.size ?: 0}")
                surveys?.surveys?.forEach { survey ->
                    println("   - ID: ${survey.id}, Title: ${survey.title}, Status: ${survey.status}, UserID: ${survey.userId}")
                }
                return surveys ?: throw Exception("Пустой ответ от сервера")
            } else {
                val errorMessage = ErrorHandler.parseError(response)
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            println("❌ Ошибка загрузки опросов врача: ${e.message}")
            throw e
        }
    }

    suspend fun fullDiagnosis() {
        println("🔍 ПОЛНАЯ ДИАГНОСТИКА ПРОБЛЕМЫ")

        try {
            val userRepo = UserRepository(context)
            val doctor = userRepo.getCurrentUser()
            println("👨‍⚕️ Текущий врач:")
            println("   - ID: ${doctor.id}")
            println("   - Email: ${doctor.email}")

            val doctorSurveys = getDoctorSurveys()
            println("📋 Опросы врача: ${doctorSurveys.surveys.size}")

            val patients = getPatients()
            println("👥 Пациенты: ${patients.patients.size}")

            val targetPatient = patients.patients.find { it.id == 6 }
            if (targetPatient != null) {
                println("🎯 Пациент ID=6 найден")

                try {
                    val attempts = getPatientSurveyAttempts(6)
                    println("📊 Попытки пациента: ${attempts.attempts.size}")

                    if (attempts.attempts.isEmpty()) {
                        println("❌ ПРИЧИНА: Пациент прошел опросы, но НЕ опросы текущего врача!")
                        println("   Опрос пациента создан user_id=4, а врач имеет user_id=${doctor.id}")
                    }
                } catch (e: Exception) {
                    println("❌ Ошибка получения попыток: ${e.message}")
                }
            }

        } catch (e: Exception) {
            println("❌ Ошибка диагностики: ${e.message}")
        }
    }

    // 🔥 НОВЫЕ МЕТОДЫ ДЛЯ РАБОТЫ С ВРАЧАМИ ПАЦИЕНТА
    suspend fun getMyDoctors(): List<User1ClientSO> {
        println("🔄 Загружаем моих врачей...")
        try {
            val response = doctorService.getMyDoctors()
            if (response.isSuccessful) {
                return response.body() ?: emptyList()
            } else {
                val errorMessage = ErrorHandler.parseError(response)
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            println("❌ Ошибка загрузки моих врачей: ${e.message}")
            throw e
        }
    }

    suspend fun getAvailableDoctors(
        query: String? = null,
        st: Int = 0,
        fn: Int? = null,
        limit: Int? = null
    ): AvailableDoctorsListResponse {
        println("🔄 Загружаем всех врачей...")
        try {
            val response = doctorService.getAvailableDoctors(query, st, fn, limit)
            if (response.isSuccessful) {
                return response.body() ?: throw Exception("Пустой ответ от сервера")
            } else {
                val errorMessage = ErrorHandler.parseError(response)
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            println("❌ Ошибка загрузки всех врачей: ${e.message}")
            throw e
        }
    }

    suspend fun associateDoctor(doctorId: Int): DoctorAssociationResponse {
        println("🔄 Привязываемся к врачу ID: $doctorId")
        try {
            val request = DoctorAssociationRequest(doctorId)
            val response = doctorService.associateDoctor(request)
            if (response.isSuccessful) {
                return response.body() ?: throw Exception("Пустой ответ от сервера")
            } else {
                val errorMessage = ErrorHandler.parseError(response)
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            println("❌ Ошибка привязки к врачу: ${e.message}")
            throw e
        }
    }

    suspend fun disassociateDoctor(doctorId: Int): DoctorAssociationResponse {
        println("🔄 Отвязываемся от врача ID: $doctorId")
        try {
            val request = DoctorAssociationRequest(doctorId)
            val response = doctorService.disassociateDoctor(request)
            if (response.isSuccessful) {
                return response.body() ?: throw Exception("Пустой ответ от сервера")
            } else {
                val errorMessage = ErrorHandler.parseError(response)
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            println("❌ Ошибка отвязки от врача: ${e.message}")
            throw e
        }
    }
}