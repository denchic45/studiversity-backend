package com.studiversity.feature.user.account.usecase

import com.studiversity.feature.user.UserRepository
import com.studiversity.transaction.SuspendTransactionWorker
import com.stuiversity.api.account.model.UpdatePasswordRequest
import java.util.*

class UpdatePasswordUseCase(
    private val transactionWorker: SuspendTransactionWorker,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: UUID, updatePasswordRequest: UpdatePasswordRequest) =
        transactionWorker.suspendInvoke {
            userRepository.update(userId, updatePasswordRequest)
        }
}