package com.studiversity.feature.course.element.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateFileRequest(
    val name: String,
    val bytes: ByteArray
) : AttachmentRequest {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreateFileRequest

        if (name != other.name) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }

}

@Serializable
data class FileItem(
    val name: String,
    val thumbnailUrl: String?
)