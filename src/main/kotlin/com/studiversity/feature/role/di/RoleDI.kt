package com.studiversity.feature.role.di

import com.studiversity.feature.role.RoleRepository
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import org.koin.dsl.module

val roleModule = module {
    includes(repositoryModule, useCaseModule)
}

private val useCaseModule = module {
    single { RequireCapabilityUseCase(get()) }
}

private val repositoryModule = module {
    single { RoleRepository() }
}
