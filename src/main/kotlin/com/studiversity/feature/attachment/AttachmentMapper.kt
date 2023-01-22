package com.studiversity.feature.attachment

import com.studiversity.database.table.AttachmentDao
import com.studiversity.database.table.Attachments
import com.studiversity.feature.course.element.model.*
import org.jetbrains.exposed.sql.ResultRow

fun AttachmentDao.toResponse() = when (type) {
    AttachmentType.FILE -> toFileAttachment()
    AttachmentType.LINK -> toLinkAttachment()
}

fun AttachmentDao.toLinkAttachment() =
    LinkAttachment(id.value, Link(url!!, name, thumbnailUrl))

fun AttachmentDao.toFileAttachment() =
    FileAttachment(id.value, FileItem(name, thumbnailUrl))

fun ResultRow.toAttachment() = when (this[Attachments.type]) {
    AttachmentType.FILE -> toFileAttachment()
    AttachmentType.LINK -> toLinkAttachment()
}

private fun ResultRow.toLinkAttachment() = LinkAttachment(
    this[Attachments.id].value,
    Link(
        url = this[Attachments.url]!!,
        name = this[Attachments.name],
        thumbnailUrl = this[Attachments.thumbnailUrl]
    )
)

private fun ResultRow.toFileAttachment() = FileAttachment(
    this[Attachments.id].value,
    FileItem(
        name = this[Attachments.name],
        thumbnailUrl = this[Attachments.thumbnailUrl]
    )
)
