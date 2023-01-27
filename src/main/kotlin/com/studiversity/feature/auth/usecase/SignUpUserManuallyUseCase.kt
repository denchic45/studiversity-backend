package com.studiversity.feature.auth.usecase

import com.stuiversity.api.auth.model.CreateUserRequest
import com.studiversity.feature.user.UserRepository
import com.studiversity.transaction.SuspendTransactionWorker

class SignUpUserManuallyUseCase(
    private val transactionWorker: SuspendTransactionWorker,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(createUserRequest: CreateUserRequest) = transactionWorker.suspendInvoke {
        userRepository.add(createUserRequest)
    }
}