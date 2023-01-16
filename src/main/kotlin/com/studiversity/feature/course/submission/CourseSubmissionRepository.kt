package com.studiversity.feature.course.submission

import com.studiversity.database.table.*
import com.studiversity.feature.course.element.CourseWorkType
import com.studiversity.feature.course.element.model.CreateFileAttachmentRequest
import com.studiversity.feature.course.submission.model.AssignmentSubmissionResponse
import com.studiversity.feature.course.submission.model.SubmissionContent
import com.studiversity.feature.course.submission.model.SubmissionResponse
import com.studiversity.feature.course.submission.model.SubmissionState
import io.github.jan.supabase.storage.BucketApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import java.util.*

class CourseSubmissionRepository(private val bucket: BucketApi) {

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

    fun submitSubmissionContent(submissionId: UUID, content: SubmissionContent?): SubmissionResponse {
        return SubmissionDao.findById(submissionId)!!.apply {
            this.content = Json.encodeToString(content)
            this.state = SubmissionState.SUBMITTED
        }.toResponse()
    }

    fun setGradeSubmission(submissionId: UUID, grade: Short, gradedBy: UUID): SubmissionResponse {
        TODO("FIX IT")
//        return SubmissionDao.findById(submissionId)!!.apply {
//            this.grade = grade
//            this.gradedBy = gradedBy
//        }.toResponse()
    }

    fun addSubmissionAttachments(submissionId: UUID, attachments: List<CreateFileAttachmentRequest>) {

    }
}