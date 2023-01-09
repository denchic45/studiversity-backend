package com.studiversity.feature.course.element.model

import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class CreateCourseElementRequest(
    val name: String,
    val description: String? = null,
    @Serializable(UUIDSerializer::class)
    val topicId: UUID? = null,
//    val order: Int? = null,
    val details: CourseElementDetails
)

