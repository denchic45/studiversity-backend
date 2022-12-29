package com.studiversity.feature.membership.usecase

import com.studiversity.feature.membership.model.Member
import com.studiversity.feature.membership.repository.UserMembershipRepository
import com.studiversity.feature.role.Role
import com.studiversity.feature.role.repository.RoleRepository
import java.util.*

class AddUserToMembershipUseCase(
    private val userMembershipRepository: UserMembershipRepository,
    private val roleRepository: RoleRepository
) {
    operator fun invoke(member: Member, roles: List<Role>, scopeId: UUID) {
        userMembershipRepository.addMember(member)
        roleRepository.addUserRolesToScope(member.userId, roles, scopeId)
    }
}