package com.example.bigproj.data.model

enum class QuestionType(val apiValue: String) {
    TEXT("text"),
    VOICE("voice"),
    PICTURE("picture"),
    COMBINED("combined")
}