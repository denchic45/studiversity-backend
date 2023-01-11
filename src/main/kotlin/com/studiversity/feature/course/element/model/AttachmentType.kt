package com.studiversity.feature.course.element.model

import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

enum class AttachmentType { File, Link }

@Serializable
data class Attachment(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    val type: AttachmentType,
    val url: String
)