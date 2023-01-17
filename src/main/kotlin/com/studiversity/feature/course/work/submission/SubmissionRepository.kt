package com.studiversity.feature.course.work.submission

import com.studiversity.database.exists
import com.studiversity.database.table.*
import com.studiversity.feature.course.element.CourseWorkType
import com.studiversity.feature.course.element.model.*
import com.studiversity.feature.course.work.submission.model.*
import com.studiversity.feature.course.work.toFileAttachment
import com.studiversity.feature.course.work.toLinkAttachment
import io.github.jan.supabase.storage.BucketApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import java.util.*

class SubmissionRepository(private val bucket: BucketApi) {

    fun addNewSubmissionByStudentId(courseWorkId: UUID, studentId: UUID): SubmissionResponse {
        return addSubmissionByStudentId(courseWorkId, studentId, SubmissionState.NEW)
    }

    fun addCreatedSubmissionByStudentId(courseWorkId: UUID, studentId: UUID): SubmissionResponse {
        return addSubmissionByStudentId(courseWorkId, studentId, SubmissionState.CREATED)
    }

    private fun addSubmissionByStudentId(
        courseWorkId: UUID,
        studentId: UUID,
        state: SubmissionState
    ): SubmissionResponse {
        return SubmissionDao.new {
            this.authorId = studentId
            this.courseWorkId = courseWorkId
            this.state = state
        }.toResponse()
    }

    fun addEmptySubmissionsByStudentIds(courseWorkId: UUID, studentIds: List<UUID>) {
        Submissions.batchInsert(studentIds) {
            set(Submissions.authorId, it)
            set(Submissions.courseWorkId, courseWorkId)
            set(Submissions.state, SubmissionState.NEW)
        }
    }

    fun find(submissionId: UUID): SubmissionResponse? {
        return SubmissionDao.findById(submissionId)?.toResponse()
    }

    fun findByStudentId(courseWorkId: UUID, studentId: UUID): SubmissionResponse? {
        return SubmissionDao.find(
            Submissions.courseWorkId eq courseWorkId and (Submissions.authorId eq studentId)
        ).singleOrNull()?.toResponse()
    }

    fun findByWorkId(courseId: UUID, courseWorkId: UUID, studentIds: List<UUID>): List<SubmissionResponse> {
        return Memberships.innerJoin(UsersMemberships, { Memberships.id }, { membershipId })
            .join(CourseWorks, JoinType.INNER, additionalConstraint = { CourseWorks.id eq courseWorkId })
            .leftJoin(
                Submissions,
                { CourseWorks.id },
                { Submissions.courseWorkId },
                { Submissions.authorId eq UsersMemberships.memberId })
            .select(Memberships.scopeId eq courseId and (UsersMemberships.memberId inList studentIds))
            .map {
                it.getOrNull(Submissions.id)?.let { submissionId ->
                    AssignmentSubmissionResponse(
                        id = submissionId.value,
                        authorId = it[Submissions.authorId],
                        state = SubmissionState.NEW,
                        courseWorkId = courseWorkId,
                        content = when (CourseWorkDao.findById(courseWorkId)!!.type) {
                            CourseWorkType.Assignment -> it[Submissions.content]
                                ?.let { content -> Json.decodeFromString(content) }
                        }
                    )
                } ?: addNewSubmissionByStudentId(courseWorkId, it[UsersMemberships.memberId])
            }
    }

    fun updateSubmissionState(submissionId: UUID, state: SubmissionState) {
        SubmissionDao.findById(submissionId)!!.state = state
    }

    fun updateSubmissionContent(submissionId: UUID, content: SubmissionContent?): SubmissionResponse? {
        return SubmissionDao.findById(submissionId)?.apply {
            this.content = Json.encodeToString(content)
        }?.toResponse()
    }

    fun submitSubmission(submissionId: UUID): SubmissionResponse {
        return SubmissionDao.findById(submissionId)!!.apply {
            this.state = SubmissionState.SUBMITTED
        }.toResponse()
    }

    fun setGradeSubmission(grade: SubmissionGrade): SubmissionResponse {
        GradeDao.new {
            this.courseId = grade.courseId
            this.studentId = SubmissionDao.findById(grade.submissionId)!!.authorId
            this.gradedBy = grade.gradedBy
            this.value = grade.value
            this.submission = SubmissionDao.findById(grade.submissionId)
        }
        return SubmissionDao.findById(grade.submissionId)!!.toResponse()
    }

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
            SubmissionsAttachments.insert {
                it[SubmissionsAttachments.submissionId] = submissionId
                it[attachmentId] = dao.id.value
            }
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
        }.also { dao ->
            SubmissionsAttachments.insert {
                it[SubmissionsAttachments.submissionId] = submissionId
                it[attachmentId] = dao.id.value
            }
        }.toLinkAttachment()
    }

    fun isAuthorBySubmissionId(submissionId: UUID, authorId: UUID): Boolean {
        return Submissions.exists { Submissions.id eq submissionId and (Submissions.authorId eq authorId) }
    }

    fun findAttachmentsBySubmissionId(submissionId: UUID): List<Attachment> {
        return Attachments.innerJoin(SubmissionsAttachments, { Attachments.id }, { attachmentId })
            .select { SubmissionsAttachments.submissionId eq submissionId }
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
}