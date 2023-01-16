package com.studiversity.database.table

import org.jetbrains.exposed.sql.Table

object SubmissionsAttachments : Table("submission_attachment") {
    val submissionId = uuid("submission_id").references(Submissions.id)
    val attachmentId = uuid("attachment_id").references(Attachments.id)

    init {
        uniqueIndex("submission_attachment_un", submissionId, attachmentId)
    }
}