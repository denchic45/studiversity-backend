package com.studiversity.feature.user.usecase

import com.studiversity.feature.user.UserRepository
import com.studiversity.transaction.SuspendTransactionWorker
import io.ktor.server.plugins.*
import java.util.*

class RemoveUserUseCase(
    private val transactionWorker: SuspendTransactionWorker,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: UUID) = transactionWorker.suspendInvoke {
        if (!userRepository.remove(userId)) throw NotFoundException()
    }
}