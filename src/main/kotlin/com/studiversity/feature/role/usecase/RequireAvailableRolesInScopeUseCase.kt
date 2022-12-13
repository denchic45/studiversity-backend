package com.studiversity.feature.role.usecase

import com.studiversity.feature.role.Role
import com.studiversity.feature.role.RoleErrors
import com.studiversity.feature.role.repository.RoleRepository
import io.ktor.server.plugins.*
import java.util.*

class RequireAvailableRolesInScopeUseCase(private val roleRepository: RoleRepository) {
    operator fun invoke(roles: List<Role>, scopeId: UUID) {
        if (!roleRepository.existRolesByScope(roles, scopeId))
            throw BadRequestException(RoleErrors.NOT_AVAILABLE_ROLE_IN_SCOPE)
    }
}