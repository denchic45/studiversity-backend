package com.studiversity.feature.attachment

import com.studiversity.database.table.AttachmentDao
import com.studiversity.feature.course.element.model.*

fun AttachmentDao.toResponse() = when (type) {
    AttachmentType.FILE -> toFileAttachment()
    AttachmentType.LINK -> toLinkAttachment()
}

fun AttachmentDao.toLinkAttachment() =
    LinkAttachment(id.value, Link(url!!, name, thumbnailUrl))

fun AttachmentDao.toFileAttachment() =
    FileAttachment(id.value, FileItem(name, thumbnailUrl))