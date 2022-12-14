package com.studiversity.feature.role.usecase

import com.studiversity.feature.role.RoleErrors
import com.studiversity.feature.role.model.UpdateUserRolesRequest
import com.studiversity.feature.role.model.UserRolesResponse
import com.studiversity.feature.role.repository.RoleRepository
import io.ktor.server.plugins.*
import java.util.*

class UpdateUserRolesInScopeUseCase(
    private val roleRepository: RoleRepository
) {
    operator fun invoke(
        userId: UUID,
        scopeId: UUID,
        updateUserRolesRequest: UpdateUserRolesRequest
    ): UserRolesResponse {
        return roleRepository.updateByUserAndScope(userId, scopeId, updateUserRolesRequest)
            ?: throw BadRequestException(RoleErrors.USER_NOT_EXIST_IN_SCOPE)
    }
}