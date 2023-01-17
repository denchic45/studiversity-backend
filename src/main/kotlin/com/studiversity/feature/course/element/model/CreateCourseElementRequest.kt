package com.studiversity.feature.course.element.model

import com.studiversity.feature.course.element.CourseWorkType
import com.studiversity.ktor.LocalDateSerializer
import com.studiversity.ktor.LocalTimeSerializer
import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.util.*


@Serializable
data class CreateCourseWorkRequest constructor(
    val name: String,
    val description: String? = null,
    @Serializable(UUIDSerializer::class)
    val topicId: UUID? = null,
    @Serializable(LocalDateSerializer::class)
    val dueDate: LocalDate?,
    @Serializable(LocalTimeSerializer::class)
    val dueTime: LocalTime?,
    val workType: CourseWorkType,
    val maxGrade: Short
)