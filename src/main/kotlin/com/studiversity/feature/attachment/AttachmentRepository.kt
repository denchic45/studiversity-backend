package com.studiversity.feature.attachment

import com.studiversity.database.table.*
import com.studiversity.feature.course.element.model.*
import com.studiversity.supabase.deleteRecursive
import io.github.jan.supabase.storage.BucketApi
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import java.util.*

class AttachmentRepository(private val bucket: BucketApi) {

    suspend fun addCourseElementFileAttachment(
        elementId: UUID,
        courseId: UUID,
        file: FileRequest
    ): FileAttachment = addFileAttachment(
        file = file,
        path = "courses/$courseId/elements/$elementId/${file.name}",
        ownerId = elementId,
        ownerType = AttachmentOwner.COURSE_ELEMENT,
        insertReferences = { addAttachmentCourseElementReference(it, elementId) }
    )

    fun addCourseElementLinkAttachment(elementId: UUID, link: LinkRequest): LinkAttachment {
        return addLinkAttachment(
            link = link,
            ownerId = elementId,
            ownerType = AttachmentOwner.COURSE_ELEMENT,
            insertReferences = { addAttachmentCourseElementReference(it, elementId) }
        )
    }

    suspend fun addSubmissionFileAttachment(
        submissionId: UUID,
        courseId: UUID,
        workId: UUID,
        fileRequest: FileRequest
    ): FileAttachment = addFileAttachment(
        file = fileRequest,
        path = "courses/$courseId/elements/$workId/submissions/$submissionId/${fileRequest.name}",
        ownerId = submissionId,
        ownerType = AttachmentOwner.SUBMISSION,
        insertReferences = { addAttachmentSubmissionReference(it, submissionId) })

    fun addSubmissionLinkAttachment(submissionId: UUID, attachment: LinkRequest): LinkAttachment {
        return addLinkAttachment(
            link = attachment,
            ownerId = submissionId,
            ownerType = AttachmentOwner.SUBMISSION,
            insertReferences = { addAttachmentSubmissionReference(it, submissionId) }
        )
    }

    private suspend fun addFileAttachment(
        file: FileRequest,
        path: String,
        ownerId: UUID,
        ownerType: AttachmentOwner,
        insertReferences: (dao: AttachmentDao) -> Unit
    ): FileAttachment = AttachmentDao.new {
        this.name = file.name
        this.type = AttachmentType.FILE
        this.path = file.path
        this.ownerId = ownerId
        this.ownerType = ownerType
    }.also { dao ->
        insertReferences(dao)
        bucket.upload(path, file.bytes)
    }.toFileAttachment()

    private fun addLinkAttachment(
        link: LinkRequest,
        ownerId: UUID,
        ownerType: AttachmentOwner,
        insertReferences: (dao: AttachmentDao) -> Unit
    ): LinkAttachment {
        return AttachmentDao.new {
            this.name = "Link name" // TODO ставить реальное название
            this.type = AttachmentType.LINK
            this.url = link.url
            this.ownerId = ownerId
            this.ownerType = ownerType
        }.also(insertReferences).toLinkAttachment()
    }


    private fun addAttachmentSubmissionReference(dao: AttachmentDao, submissionId: UUID) {
        AttachmentsSubmissions.insert {
            it[attachmentId] = dao.id.value
            it[AttachmentsSubmissions.submissionId] = submissionId
        }
    }

    private fun addAttachmentCourseElementReference(dao: AttachmentDao, elementId: UUID) {
        AttachmentsCourseElements.insert {
            it[attachmentId] = dao.id.value
            it[courseElementId] = elementId
        }
    }

    fun findAttachmentsBySubmissionId(submissionId: UUID): List<Attachment> {
        return Attachments.innerJoin(AttachmentsSubmissions, { Attachments.id }, { attachmentId })
            .select { AttachmentsSubmissions.submissionId eq submissionId }
            .map {
                when (it[Attachments.type]) {
                    AttachmentType.FILE -> {
                        FileAttachment(
                            it[Attachments.id].value,
                            FileItem(
                                name = it[Attachments.name],
                                thumbnailUrl = it[Attachments.thumbnailUrl]
                            )
                        )
                    }

                    AttachmentType.LINK -> {
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

    suspend fun removeByCourseId(courseId: UUID) {
        val elementIds = CourseElements.slice(CourseElements.id)
            .select(CourseElements.courseId eq courseId)
            .map { it[CourseElements.id].value }

        val submissionIds = Submissions.slice(Submissions.id)
            .select(Submissions.courseWorkId inList elementIds)
            .map { it[Submissions.id].value }

        removeByOwnerIds(elementIds + submissionIds)

        bucket.deleteRecursive("courses/$courseId")
    }

    suspend fun removeByCourseWorkId(courseId: UUID, workId: UUID) {
        removeByCourseElementId(courseId, workId)
        removeByOwnerIds(
            Submissions.slice(Submissions.id)
                .select(Submissions.courseWorkId eq workId)
                .map { it[Submissions.id].value }
        )
    }

    private suspend fun removeByCourseElementId(courseId: UUID, elementId: UUID) {
        removeByOwnerId(elementId)
        bucket.deleteRecursive("courses/$courseId/elements/$elementId")
    }

    private fun removeByOwnerId(ownerId: UUID) {
        Attachments.deleteWhere { Attachments.ownerId eq ownerId }
    }

    private fun removeByOwnerIds(ownerIds: List<UUID>) {
        Attachments.deleteWhere { ownerId inList ownerIds }
    }

    suspend fun removeBySubmissionId(courseId: UUID, elementId: UUID, submissionId: UUID, attachmentId: UUID): Boolean {
        val attachmentDao = AttachmentDao.findById(attachmentId) ?: return false
        if (attachmentDao.ownerId == submissionId) {
            removeByOwnerId(submissionId)
            if (attachmentDao.type == AttachmentType.FILE)
                bucket.delete("courses/$courseId/elements/$elementId/submissions/$submissionId/${attachmentDao.name}")
        } else {
            AttachmentsSubmissions.deleteWhere {
                AttachmentsSubmissions.submissionId eq submissionId and (AttachmentsSubmissions.attachmentId eq attachmentId)
            }
        }
        return true
    }
}