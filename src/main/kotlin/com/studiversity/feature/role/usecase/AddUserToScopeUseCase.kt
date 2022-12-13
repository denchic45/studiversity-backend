package com.studiversity.feature.role.usecase

import com.studiversity.feature.role.Role
import com.studiversity.feature.role.RoleErrors
import com.studiversity.feature.role.repository.RoleRepository
import io.ktor.server.plugins.*
import java.util.*

class AddUserToScopeUseCase(private val roleRepository: RoleRepository) {

    operator fun invoke(userId: UUID, scopeId: UUID, roles: List<Role>) {
        if (roleRepository.existUserByScope(userId, scopeId))
            throw BadRequestException(RoleErrors.USER_ALREADY_EXIST_IN_SCOPE)
        roleRepository.addByUserAndScope(userId, scopeId, roles)
    }
}