// domain/repository/DoctorRepository.kt
package com.example.bigproj.domain.repository

import android.content.Context
import com.example.bigproj.data.RetrofitClient
import com.example.bigproj.domain.utils.ErrorHandler

class DoctorRepository(private val context: Context) {

    private val tokenManager = TokenManager(context)
    private val doctorService by lazy {
        RetrofitClient.createDoctorService(tokenManager)
    }

    suspend fun getPatients(): com.example.bigproj.data.model.PatientsListResponse {
        println("👥 ДИАГНОСТИКА: Загружаем список пациентов через /api/doctor/my_patients")

        try {
            // 🔥 ИСПОЛЬЗУЕМ НОВЫЙ ЭНДПОИНТ
            val response = doctorService.getMyPatients()

            println("📡 Ответ my_patients: код=${response.code()}, успешно=${response.isSuccessful}")
            println("📊 Полный ответ пациентов: ${response.body()}") // 🔥 ДОБАВЛЕНО

            if (response.isSuccessful) {
                val patientsResponse = response.body()
                println("✅ Пациенты загружены: ${patientsResponse?.patients?.size ?: 0}")

                // 🔥 ПРОВЕРКА ПРИВЯЗКИ КОНКРЕТНОГО ПАЦИЕНТА (ДОБАВЛЕНО)
                val targetPatient = patientsResponse?.patients?.find { it.id == 6 }
                println("🎯 Поиск пациента ID=6: ${if (targetPatient != null) "НАЙДЕН" else "НЕ НАЙДЕН"}")
                println("🎯 Данные пациента ID=6: $targetPatient")

                // 🔥 ВЫВОДИМ ВСЕХ ПАЦИЕНТОВ ДЛЯ ДЕБАГА
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

                // 🔥 ДЕТАЛЬНЫЙ АНАЛИЗ ОШИБОК
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

    suspend fun getPatientSurveyAttempts(patientId: Int): com.example.bigproj.data.model.PatientAttemptsResponse {
        println("📊 ДИАГНОСТИКА: Загружаем ответы пациента ID: $patientId")

        try {
            // 🔥 ИСПОЛЬЗУЕМ НОВЫЙ ЭНДПОИНТ
            val response = doctorService.getPatientAttempts(patientId)

            println("📡 Ответ patient_attempts: код=${response.code()}, успешно=${response.isSuccessful}")
            println("📡 Полный ответ: ${response.body()}") // 🔥 ДОБАВЛЕНО

            if (response.isSuccessful) {
                val attempts = response.body()

                // 🔥 ДЕТАЛЬНАЯ ДИАГНОСТИКА (ДОБАВЛЕНО)
                println("✅ Ответы пациента загружены:")
                println("   - attempts: ${attempts?.attempts?.size ?: 0}")
                println("   - patientInfo: ${attempts?.patientInfo}")
                println("   - totalCount: ${attempts?.totalCount}")
                println("   - returnedCount: ${attempts?.returnedCount}")

                // 🔥 ПРОВЕРКА СТРУКТУРЫ ОТВЕТА (ДОБАВЛЕНО)
                if (attempts != null) {
                    println("🔍 Структура ответа:")
                    println("   - attempts exists: ${attempts.attempts != null}")
                    println("   - attempts is list: ${attempts.attempts is List<*>}")
                    attempts.attempts?.let {
                        println("   - attempts class: ${it.javaClass.simpleName}")
                    }
                }

                // 🔥 ДЕТАЛЬНАЯ ИНФОРМАЦИЯ О ПОПЫТКАХ
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

    suspend fun getDoctorSurveys(): com.example.bigproj.data.model.SurveyListResponseDto {
        println("📋 Получаем опросы текущего врача")

        try {
            val response = doctorService.getMySurveys()

            println("📡 Ответ my_surveys: код=${response.code()}, успешно=${response.isSuccessful}")
            println("📡 Тело: ${response.body()}")

            if (response.isSuccessful) {
                val surveys = response.body()
                println("✅ Опросы врача: ${surveys?.surveys?.size ?: 0}")
                surveys?.surveys?.forEach { survey ->
                    println("   - ID: ${survey.id}, Title: ${survey.title}, UserID: ${survey.userId}")
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
            // 1. Кто текущий врач
            val userRepo = UserRepository(context)
            val doctor = userRepo.getCurrentUser()
            println("👨‍⚕️ Текущий врач:")
            println("   - ID: ${doctor.id}")
            println("   - Email: ${doctor.email}")

            // 2. Какие опросы у врача
            val doctorSurveys = getDoctorSurveys()
            println("📋 Опросы врача: ${doctorSurveys.surveys.size}")

            // 3. Какие пациенты
            val patients = getPatients()
            println("👥 Пациенты: ${patients.patients.size}")

            // 4. Проверим конкретного пациента
            val targetPatient = patients.patients.find { it.id == 6 }
            if (targetPatient != null) {
                println("🎯 Пациент ID=6 найден")

                // 5. Проверим попытки
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
}