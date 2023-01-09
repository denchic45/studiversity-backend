package com.studiversity.feature.course.model

import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class CreateCourseRequest(
    val name: String,
    @Serializable(UUIDSerializer::class)
    val subjectId: UUID? = null,
)