package com.studiversity.feature.course.element.model

import com.studiversity.feature.course.element.CourseElementType
import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable(CourseElementSerializer::class)
sealed interface CreateCourseElementRequest {
    val name: String
    val description: String?
    val topicId: UUID?
    val type: CourseElementType
    val details: CourseElementDetails
}

@Serializable
data class CreateCourseWorkRequest(
    override val name: String,
    override val description: String? = null,
    @Serializable(UUIDSerializer::class)
    override val topicId: UUID? = null,
    override val details: CourseWork
) : CreateCourseElementRequest {
    override val type: CourseElementType = CourseElementType.Work
}