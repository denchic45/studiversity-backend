package com.studiversity.feature.studygroup.usecase

import com.studiversity.feature.role.Role
import com.studiversity.feature.role.RoleRepository
import com.studiversity.feature.studygroup.StudyGroupErrors
import com.studiversity.feature.studygroup.repository.StudyGroupMemberRepository
import io.ktor.server.plugins.*
import java.util.*

class EnrollStudyGroupMemberUseCase(
    private val roleRepository: RoleRepository,
    private val studyGroupMemberRepository: StudyGroupMemberRepository
) {

    operator fun invoke(groupId: UUID, userId: UUID, roles: List<Role>) {
        if (roleRepository.existUserByScope(userId, groupId))
            throw BadRequestException(StudyGroupErrors.MEMBER_ALREADY_ENROLLED_IN_GROUP)
        studyGroupMemberRepository.add(groupId, userId, roles)
    }
}