package com.example.firebase_lsm_pp.models

data class LessonVideo(
    val text: String = "",
    val video: String = ""
)

data class LessonOption(
    val isCorrect: Boolean = false,
    val optionA: String? = null,
    val optionB: String? = null,
    val optionC: String? = null,
    val video: String? = null
)

data class LessonQuestion(
    val text: String = "",
    val options: List<LessonOption> = emptyList()
)


data class Lesson(
    val title: String = "",
    val description: String = "",
    val thumbnail: String = "",
    val lessonVideos: List<LessonVideo> = emptyList(),
    val question: LessonQuestion = LessonQuestion(),
    val points: Int = 0
)
