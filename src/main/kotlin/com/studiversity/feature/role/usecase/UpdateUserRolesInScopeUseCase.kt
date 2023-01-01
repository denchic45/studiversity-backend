package com.studiversity.feature.role.usecase

import com.studiversity.feature.role.model.UpdateUserRolesRequest
import com.studiversity.feature.role.model.UserRolesResponse
import com.studiversity.feature.role.repository.RoleRepository
import com.studiversity.transaction.TransactionWorker
import java.util.*

class UpdateUserRolesInScopeUseCase(
    private val transactionWorker: TransactionWorker,
    private val roleRepository: RoleRepository
) {
    operator fun invoke(
        userId: UUID,
        scopeId: UUID,
        updateUserRolesRequest: UpdateUserRolesRequest
    ): UserRolesResponse = transactionWorker {
        roleRepository.updateByUserAndScope(userId, scopeId, updateUserRolesRequest)
    }
}