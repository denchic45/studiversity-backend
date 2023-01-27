package com.studiversity.feature.auth.usecase

import com.stuiversity.api.auth.model.SignupRequest
import com.studiversity.feature.user.UserRepository
import com.studiversity.transaction.SuspendTransactionWorker

class SignUpUseCase(
    private val transactionWorker: SuspendTransactionWorker,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(signupRequest: SignupRequest) = transactionWorker.suspendInvoke {
        userRepository.add(signupRequest)
    }
}