package com.studiversity.feature.role.usecase

import com.studiversity.feature.role.model.UserWithRolesResponse
import com.studiversity.feature.role.repository.RoleRepository
import java.util.*

class FindUsersInScopeUseCase(private val roleRepository: RoleRepository) {
    operator fun invoke(scopeId: UUID): List<UserWithRolesResponse> {
        return roleRepository.findUsersByScopeId(scopeId)
    }
}