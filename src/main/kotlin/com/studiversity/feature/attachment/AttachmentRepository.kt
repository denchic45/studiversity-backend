package com.studiversity.feature.attachment

import com.studiversity.database.exists
import com.studiversity.database.table.*
import com.studiversity.feature.course.element.model.*
import com.studiversity.feature.course.work.toFileAttachment
import com.studiversity.feature.course.work.toLinkAttachment
import com.studiversity.supabase.deleteRecursive
import io.github.jan.supabase.storage.BucketApi
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import java.util.*

class AttachmentRepository(
    private val bucket: BucketApi
) {

    suspend fun addSubmissionFileAttachment(
        submissionId: UUID,
        courseId: UUID,
        workId: UUID,
        fileRequest: FileRequest
    ): FileAttachment {
        return AttachmentDao.new {
            this.name = fileRequest.name
            this.type = AttachmentType.File
            this.path = fileRequest.path
        }.also { dao ->
            addAttachmentSubmission(dao, submissionId)
            bucket.upload(
                "courses/$courseId/elements/$workId/submissions/$submissionId/${fileRequest.name}",
                fileRequest.bytes
            )
        }.toFileAttachment()
    }

    fun addSubmissionLinkAttachment(submissionId: UUID, attachment: LinkRequest): LinkAttachment {
        return AttachmentDao.new {
            this.name = "Link name" // TODO ставить реальное название
            this.type = AttachmentType.Link
            this.url = attachment.url
        }.also { dao -> addAttachmentSubmission(dao, submissionId) }.toLinkAttachment()
    }

    private fun addAttachmentSubmission(dao: AttachmentDao, submissionId: UUID) {
        AttachmentsSubmissions.insert {
            it[attachmentId] = dao.id.value
            it[AttachmentsSubmissions.submissionId] = submissionId
        }
    }

    fun findAttachmentsBySubmissionId(submissionId: UUID): List<Attachment> {
        return Attachments.innerJoin(AttachmentsSubmissions, { Attachments.id }, { attachmentId })
            .select { AttachmentsSubmissions.submissionId eq submissionId }
            .map {
                when (it[Attachments.type]) {
                    AttachmentType.File -> {
                        FileAttachment(
                            it[Attachments.id].value,
                            FileItem(
                                name = it[Attachments.name],
                                thumbnailUrl = it[Attachments.thumbnailUrl]
                            )
                        )
                    }

                    AttachmentType.Link -> {
                        LinkAttachment(
                            it[Attachments.id].value,
                            Link(
                                url = it[Attachments.url]!!,
                                name = it[Attachments.name],
                                thumbnailUrl = it[Attachments.thumbnailUrl]
                            )
                        )
                    }
                }
            }
    }

    suspend fun removeByCourseWorkId(courseId: UUID, workId: UUID) {
        removeByCourseElementId(courseId, workId)
        removeBySubmissionIds(
            Submissions.slice(Submissions.id)
                .select(Submissions.courseWorkId eq workId)
                .map { it[Submissions.id].value }
        )
    }

    private suspend fun removeByCourseElementId(courseId: UUID, elementId: UUID) {
        val ids = Attachments.innerJoin(AttachmentsCourseElements, { id }, { attachmentId })
            .slice(Attachments.id)
            .select(AttachmentsCourseElements.courseElementId eq elementId)
            .map { it[Attachments.id] }
        removeByIds(ids)
        bucket.deleteRecursive("courses/$courseId/elements/$elementId")
    }

    private fun removeBySubmissionIds(submissionIds: List<UUID>) {
        val ids = Attachments.innerJoin(AttachmentsSubmissions, { Attachments.id }, { attachmentId })
            .slice(Attachments.id)
            .select(AttachmentsSubmissions.submissionId inList submissionIds)
            .map { it[Attachments.id] }
        removeByIds(ids)
    }

    private fun removeByIds(ids: List<EntityID<UUID>>) {
        if (ids.isEmpty()) return
        Attachments.deleteWhere { id inList ids }
    }
}