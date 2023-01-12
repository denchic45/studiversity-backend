package com.studiversity.feature.course.submission.usecase

import com.studiversity.feature.course.element.repository.CourseElementRepository
import com.studiversity.feature.course.submission.CourseSubmissionRepository
import com.studiversity.feature.course.submission.model.SubmissionState
import com.studiversity.feature.membership.repository.UserMembershipRepository
import com.studiversity.feature.role.Role
import com.studiversity.transaction.TransactionWorker
import io.ktor.server.plugins.*
import java.util.*

class FindSubmissionUseCase(
    private val transactionWorker: TransactionWorker,
    private val submissionRepository: CourseSubmissionRepository,
    private val courseElementRepository: CourseElementRepository,
    private val userMembershipRepository: UserMembershipRepository
) {

    operator fun invoke(submissionId: UUID, courseWorkId: UUID, receivingUserId: UUID) = transactionWorker {
        submissionRepository.find(submissionId).let { response ->
            val courseId = courseElementRepository.findCourseIdByElementId(courseWorkId)
                ?: throw NotFoundException()

            if (response != null) {
                if (response.state == SubmissionState.NEW) {
                    submissionRepository.updateSubmissionState(submissionId, SubmissionState.CREATED)
                    submissionRepository.find(submissionId)!!
                } else response
            } else if (userMembershipRepository.existMemberByScopeIdAndRole(
                    memberId = receivingUserId,
                    scopeId = courseId,
                    roleId = Role.Student.id
                )
            ) submissionRepository.addCreatedSubmissionByStudentId(courseWorkId, receivingUserId)
            else throw NotFoundException()
        }
    }
}