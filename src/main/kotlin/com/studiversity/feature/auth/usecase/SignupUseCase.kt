package com.studiversity.feature.auth.usecase

import com.studiversity.feature.auth.model.SignupRequest
import com.studiversity.feature.user.UserRepository
import com.studiversity.transaction.SuspendTransactionWorker

class SignupUseCase(
    private val transactionWorker: SuspendTransactionWorker,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(signupRequest: SignupRequest) = transactionWorker.suspendInvoke {
        userRepository.add(signupRequest)
    }
}