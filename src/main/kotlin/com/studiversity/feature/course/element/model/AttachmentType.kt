package com.studiversity.feature.course.element.model

import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import java.util.*

enum class AttachmentType { FILE, LINK }

enum class AttachmentOwner { SUBMISSION, COURSE_ELEMENT }

@Serializable(AttachmentSerializer::class)
sealed class Attachment {
    abstract val id: UUID
    abstract val type: AttachmentType
}

@Serializable
data class FileAttachment(
    @Serializable(UUIDSerializer::class)
    override val id: UUID,
    val fileItem: FileItem
) : Attachment() {
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault
    override val type: AttachmentType = AttachmentType.FILE
}

@Serializable
data class LinkAttachment(
    @Serializable(UUIDSerializer::class)
    override val id: UUID,
    val link: Link
) : Attachment() {
    @OptIn(ExperimentalSerializationApi::class)
    @EncodeDefault
    override val type: AttachmentType = AttachmentType.LINK
}