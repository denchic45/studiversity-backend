package com.studiversity.database.table

import com.studiversity.feature.course.element.model.AttachmentType
import com.studiversity.util.varcharMax
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

object Attachments : UUIDTable("attachment", "attachment_id") {
    val name = varcharMax("attachment_name")
    val url = varcharMax("url")
    val thumbnailUrl = varcharMax("thumbnail_url").nullable()
    var type = enumerationByName<AttachmentType>("type", 8)
    var path = varcharMax("path").nullable()
}

class AttachmentDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AttachmentDao>(Attachments)

    var name by Attachments.name
    var url by Attachments.url
    var thumbnailUrl by Attachments.thumbnailUrl
    var type by Attachments.type
    var path by Attachments.path
}