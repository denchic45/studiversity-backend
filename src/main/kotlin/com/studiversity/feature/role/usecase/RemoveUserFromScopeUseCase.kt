package com.studiversity.feature.role.usecase

import com.studiversity.feature.role.RoleErrors
import com.studiversity.feature.role.repository.RoleRepository
import io.ktor.server.plugins.*
import java.util.*

class RemoveUserFromScopeUseCase(private val roleRepository: RoleRepository) {
    operator fun invoke(userId: UUID, scopeId: UUID) {
        if (!roleRepository.existUserByScope(userId, scopeId)) {
            throw BadRequestException(RoleErrors.USER_NOT_EXIST_IN_SCOPE)
        }
        roleRepository.removeByUserAndScope(userId, scopeId)
    }
}