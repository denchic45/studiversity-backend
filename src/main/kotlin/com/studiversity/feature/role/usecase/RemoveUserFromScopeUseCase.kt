package com.studiversity.feature.role.usecase

import com.studiversity.feature.membership.repository.UserMembershipRepository
import com.studiversity.feature.role.RoleErrors
import com.studiversity.feature.role.repository.RoleRepository
import com.studiversity.ktor.ConflictException
import io.ktor.server.plugins.*
import java.util.*

class RemoveUserFromScopeUseCase(
    private val userMembershipRepository: UserMembershipRepository,
    private val roleRepository: RoleRepository
) {
    operator fun invoke(userId: UUID, scopeId: UUID) {
        val externalMembershipTypes = listOf("by_group") // Types who prevent remove member
        val memberships =
            userMembershipRepository.findMemberByMembershipTypesAndScopeId(userId, externalMembershipTypes, scopeId)
        if (memberships.isNotEmpty())
            throw ConflictException("MEMBER_IS_IN_EXTERNAL_MEMBERSHIP")
        if (!roleRepository.existUserByScope(userId, scopeId)) {
            throw BadRequestException(RoleErrors.USER_NOT_EXIST_IN_SCOPE)
        }
        roleRepository.removeUserRolesFromScope(userId, scopeId)
    }
}