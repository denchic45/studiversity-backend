package com.studiversity.feature.course.element.model

import com.studiversity.feature.course.element.CourseWorkType
import com.studiversity.ktor.LocalDateSerializer
import com.studiversity.ktor.LocalTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime

@Serializable
sealed class CourseElementDetails

@Serializable
data class CourseWork(
    @Serializable(LocalDateSerializer::class)
    val dueDate: LocalDate?,
    @Serializable(LocalTimeSerializer::class)
    val dueTime: LocalTime?,
    val workType: CourseWorkType,
    val workDetails: CourseWorkDetails? = null
) : CourseElementDetails()

@Serializable
data class CourseMaterial(
    val text: String
) : CourseElementDetails()