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
}

@Serializable
data class CreateCourseWorkRequest(
    override val name: String,
    override val description: String? = null,
    @Serializable(UUIDSerializer::class)
    override val topicId: UUID? = null,
) : CreateCourseElementRequest {
    override val type: CourseElementType = CourseElementType.Work
}