package com.studiversity.feature.auth.usecase

import com.studiversity.feature.auth.model.CreateUserRequest
import com.studiversity.transaction.TransactionWorker

class RegisterUserManuallyUseCase(private val transactionWorker: TransactionWorker) {
    operator fun invoke(createUserRequest: CreateUserRequest) = transactionWorker {

    }
}