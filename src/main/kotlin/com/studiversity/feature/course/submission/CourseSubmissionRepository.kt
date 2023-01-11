package com.studiversity.feature.course.submission

import com.studiversity.database.table.*
import com.studiversity.feature.course.submission.model.Submission
import com.studiversity.feature.course.submission.model.SubmissionState
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.util.*

class CourseSubmissionRepository {

    fun addEmptySubmissionByStudentIds(courseWorkId: UUID, studentIds: List<UUID>) {
        Submissions.batchInsert(studentIds) {
            set(Submissions.authorId, it)
            set(Submissions.courseWorkId, courseWorkId)
            set(Submissions.state, SubmissionState.NEW)
        }
    }

    fun find(submissionId: UUID): Submission? {
        return SubmissionDao.findById(submissionId)?.toResponse()
    }

    fun findByWorkId(courseId: UUID, courseWorkId: UUID) {
        Memberships.innerJoin(UsersMemberships, { Memberships.id }, { membershipId })
            .innerJoin(
                UsersRolesScopes,
                { Memberships.scopeId },
                { scopeId },
                { UsersRolesScopes.userId eq UsersMemberships.memberId })
            .leftJoin(Submissions, { UsersMemberships.memberId }, { authorId })
            .select(Memberships.scopeId eq courseId and (Submissions.courseWorkId eq courseWorkId))
    }
}