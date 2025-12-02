package com.example.firebase_lsm_pp.models

data class LessonOption(
    val isCorrect: Boolean = false,
    val optionA: String? = null,
    val optionB: String? = null,
    val optionC: String? = null
)

data class LessonQuestion(
    val text: String = "",
    val options: List<LessonOption> = emptyList()
)

data class Lesson(
    val title: String = "",
    val thumbnail: String = "",
    val video: String = "",
    val question: LessonQuestion = LessonQuestion()
)

