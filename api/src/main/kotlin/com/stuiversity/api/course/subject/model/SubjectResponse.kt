package com.stuiversity.api.course.subject.model

import com.stuiversity.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class SubjectResponse(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    val name: String,
    val iconName: String
)
