package com.studiversity.feature.role.usecase

import com.studiversity.feature.role.RoleErrors
import com.studiversity.feature.role.RoleRepository
import io.ktor.server.plugins.*
import java.util.*

class RequireAllowRoleInScopeUseCase(private val roleRepository: RoleRepository) {
    operator fun invoke(roles: List<String>, scopeId: UUID) {
        if (!roleRepository.existRolesByScope(roles, scopeId))
            throw BadRequestException(RoleErrors.NOT_ALLOWED_ROLE_IN_SCOPE)
    }
}