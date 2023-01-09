package com.studiversity.feature.course.model

import com.studiversity.feature.course.subject.model.SubjectResponse
import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class CourseResponse(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val subject: SubjectResponse?,
    val archived: Boolean
)
