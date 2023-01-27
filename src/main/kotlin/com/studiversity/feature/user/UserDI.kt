package com.studiversity.feature.user

import com.studiversity.feature.user.usecase.FindUserByIdUseCase
import com.studiversity.feature.user.usecase.RemoveUserUseCase
import org.koin.dsl.module

private val useCaseModule = module {
    single { FindUserByIdUseCase(get(), get()) }
    single { RemoveUserUseCase(get(), get()) }
}

private val repositoryModule = module {
    single { UserRepository(get()) }
}

val userModule = module {
    includes(useCaseModule, repositoryModule)
}