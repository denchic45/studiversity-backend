package com.studiversity.feature.course.element.model

import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

enum class AttachmentType { File, Link }

@Serializable
sealed class Attachment() {
    @Serializable(UUIDSerializer::class)
    abstract val id: UUID
    abstract val type: AttachmentType
    abstract val url: String
}

data class FileAttachment(override val id: UUID, override val type: AttachmentType, override val url: String) :
    Attachment()