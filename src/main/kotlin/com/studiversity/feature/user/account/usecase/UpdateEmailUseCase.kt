package com.studiversity.feature.user.account.usecase

import com.studiversity.feature.user.UserRepository
import com.studiversity.transaction.SuspendTransactionWorker
import com.stuiversity.api.account.model.UpdateEmailRequest
import java.util.*

class UpdateEmailUseCase(
    private val transactionWorker: SuspendTransactionWorker,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId:UUID, updateEmailRequest: UpdateEmailRequest) = transactionWorker.suspendInvoke {
        userRepository.update(userId, updateEmailRequest)
    }
}