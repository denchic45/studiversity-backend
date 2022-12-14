package com.studiversity.feature.role.usecase

import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.repository.RoleRepository
import com.studiversity.ktor.ForbiddenException
import java.util.*

class RequireCapabilityUseCase(private val roleRepository: RoleRepository) {

    operator fun invoke(userId: UUID, capability: Capability, scopeId: UUID) {
        if (!roleRepository.hasCapability(userId, capability, scopeId)) {
            throw ForbiddenException()
        }
    }
}