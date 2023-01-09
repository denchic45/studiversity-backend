package com.studiversity.feature.courseelement.model

import com.studiversity.feature.courseelement.WorkType
import com.studiversity.ktor.LocalDateSerializer
import com.studiversity.ktor.LocalTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime

@Serializable
sealed class CourseElementDetails {
    @Serializable
    @SerialName("Work")
    data class Work(
        @Serializable(LocalDateSerializer::class)
        val dueDate: LocalDate? = null,
        @Serializable(LocalTimeSerializer::class)
        val dueTime: LocalTime? = null,
        val workType: WorkType
    ) : CourseElementDetails()

    @Serializable
    @SerialName("Post")
    data class Post(
        val content: String
    ) : CourseElementDetails()
}