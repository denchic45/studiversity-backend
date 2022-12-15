package com.studiversity.feature.course.subject.model

import com.studiversity.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class SubjectResponse(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val iconName: String
)
