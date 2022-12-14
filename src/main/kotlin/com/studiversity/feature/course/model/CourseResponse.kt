package com.studiversity.feature.course.model

import com.studiversity.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class CourseResponse(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    val name: String
)
