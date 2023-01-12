package com.studiversity.feature.course.submission

import com.studiversity.database.table.*
import com.studiversity.feature.course.element.CourseWorkType
import com.studiversity.feature.course.submission.model.SubmissionResponse
import com.studiversity.feature.course.submission.model.SubmissionState
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class CourseSubmissionRepository {

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

    fun findByWorkId(courseId: UUID, courseWorkId: UUID): List<SubmissionResponse> {
        return Memberships.innerJoin(UsersMemberships, { Memberships.id }, { membershipId })
            .innerJoin(
                UsersRolesScopes,
                { Memberships.scopeId },
                { scopeId },
                { UsersRolesScopes.userId eq UsersMemberships.memberId })
            .leftJoin(Submissions, { UsersMemberships.memberId }, { authorId })
            .select(Memberships.scopeId eq courseId and (Submissions.courseWorkId eq courseWorkId))
            .map {
                it.getOrNull(Submissions.id)?.let { submissionId ->
                    SubmissionResponse(
                        id = submissionId.value,
                        authorId = it[Submissions.authorId],
                        state = SubmissionState.NEW,
                        courseWorkId = courseWorkId,
                        content = when (CourseWorkDao.findById(courseWorkId)!!.type) {
                            CourseWorkType.Assignment -> it[Submissions.content]
                                ?.let { content -> Json.decodeFromString(content) }
                        }
                    )
                } ?: addNewSubmissionByStudentId(courseWorkId, it[UsersMemberships.memberId].value)
            }
    }

    fun updateSubmissionState(submissionId: UUID, state: SubmissionState) {
        SubmissionDao.findById(submissionId)!!.state = state
    }
}