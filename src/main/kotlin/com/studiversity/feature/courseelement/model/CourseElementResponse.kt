package com.studiversity.feature.courseelement.model

import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

data class CourseElementResponse(
    @Serializable(UUIDSerializer::class)
    val courseId: UUID,
    @Serializable(UUIDSerializer::class)
    val topicId: UUID?,
    val order: Int? = null,
    val details: CourseElementDetails
)