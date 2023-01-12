package com.studiversity.feature.course.element.usecase

import com.studiversity.feature.course.element.model.CreateCourseElementRequest
import com.studiversity.feature.course.element.repository.CourseElementRepository
import com.studiversity.feature.course.submission.CourseSubmissionRepository
import com.studiversity.feature.membership.repository.UserMembershipRepository
import com.studiversity.feature.role.Role
import com.studiversity.transaction.TransactionWorker
import java.util.*

class AddCourseWorkUseCase(
    private val transactionWorker: TransactionWorker,
    private val courseElementRepository: CourseElementRepository,
    private val userMembershipRepository: UserMembershipRepository,
    private val courseSubmissionRepository: CourseSubmissionRepository
) {
    operator fun invoke(courseId: UUID, request: CreateCourseElementRequest) = transactionWorker {
        val response = courseElementRepository.add(courseId, request)
        val studentIds = userMembershipRepository.findMemberIdsByScopeAndRole(courseId, Role.Student.id)
        courseSubmissionRepository.addEmptySubmissionsByStudentIds(response.id, studentIds)
        response
    }
}