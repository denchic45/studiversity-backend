package com.studiversity.feature.courseelement.model

import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class CreateCourseElementRequest(
    @Serializable(UUIDSerializer::class)
    val topicId: UUID,
    val order: Int? = null,
    val details: CourseElementDetails
)

