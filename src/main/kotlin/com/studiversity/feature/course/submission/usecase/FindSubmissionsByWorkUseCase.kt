package com.studiversity.feature.course.submission.usecase

import com.studiversity.feature.course.submission.CourseSubmissionRepository
import com.studiversity.feature.membership.repository.UserMembershipRepository
import com.studiversity.feature.role.Role
import com.studiversity.transaction.TransactionWorker
import java.util.*

class FindSubmissionsByWorkUseCase(
    private val transactionWorker: TransactionWorker,
    private val userMembershipRepository: UserMembershipRepository,
    private val submissionRepository: CourseSubmissionRepository
) {

    operator fun invoke(courseId: UUID, courseWorkId: UUID) = transactionWorker {
        submissionRepository.findByWorkId(
            courseId,
            courseWorkId,
            userMembershipRepository.findMemberIdsByScopeAndRole(courseId, Role.Student.id)
        )
    }
}