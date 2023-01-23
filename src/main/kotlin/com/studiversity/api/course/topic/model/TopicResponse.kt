package com.studiversity.api.course.topic.model

import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class TopicResponse(
    val id: @Serializable(UUIDSerializer::class) UUID,
    val name: String
)
