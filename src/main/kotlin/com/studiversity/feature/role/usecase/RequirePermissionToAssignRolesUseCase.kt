package com.studiversity.feature.role.usecase

import com.studiversity.feature.role.ForbiddenException
import com.studiversity.feature.role.Role
import com.studiversity.feature.role.RoleErrors
import com.studiversity.feature.role.repository.RoleRepository
import java.util.*

class RequirePermissionToAssignRolesUseCase(private val roleRepository: RoleRepository) {

    operator fun invoke(userId: UUID, roles: List<Role>, scopeId: UUID) {
        if (roleRepository.existPermissionRolesByUserId(userId, roles, scopeId))
            throw ForbiddenException(RoleErrors.PERMISSION_DENIED_TO_ASSIGN_ROLE)
    }
}