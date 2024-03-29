package com.studiversity.feature.course.element.model2

import com.stuiversity.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class CourseElementDetails(
    @Serializable(UUIDSerializer::class)
    val courseId: UUID,
    val name: String,
    val description: String? = null,
    @Serializable(UUIDSerializer::class)
    val topicId: UUID?,
    val order: Int? = null
)