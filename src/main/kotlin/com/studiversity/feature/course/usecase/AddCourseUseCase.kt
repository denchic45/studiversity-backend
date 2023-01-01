package com.studiversity.feature.course.usecase

import com.studiversity.Constants
import com.studiversity.feature.course.model.CreateCourseRequest
import com.studiversity.feature.course.repository.CourseRepository
import com.studiversity.feature.membership.model.CreateMembershipRequest
import com.studiversity.feature.membership.repository.MembershipRepository
import com.studiversity.feature.role.ScopeType
import com.studiversity.feature.role.repository.ScopeRepository
import com.studiversity.transaction.TransactionWorker
import java.util.*

class AddCourseUseCase(
    private val transactionWorker: TransactionWorker,
    private val courseRepository: CourseRepository,
    private val scopeRepository: ScopeRepository,
    private val membershipRepository: MembershipRepository
) {
    operator fun invoke(request: CreateCourseRequest): UUID = transactionWorker {
        courseRepository.add(request).also { id ->
            scopeRepository.add(id, ScopeType.Course, Constants.organizationId)
            membershipRepository.addManualMembership(CreateMembershipRequest("manual", id))
        }
    }
}