package com.studiversity.feature.role.di

import com.studiversity.feature.role.RoleRepository
import com.studiversity.feature.role.usecase.FindRolesByNamesUseCase
import com.studiversity.feature.role.usecase.RequireAvailableRolesInScopeUseCase
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import com.studiversity.feature.role.usecase.RequirePermissionToAssignRolesUseCase
import org.koin.dsl.module

val roleModule = module {
    includes(repositoryModule, useCaseModule)
}

private val useCaseModule = module {
    single { RequireCapabilityUseCase(get()) }
    single { RequireAvailableRolesInScopeUseCase(get()) }
    single { RequirePermissionToAssignRolesUseCase(get()) }
    single { FindRolesByNamesUseCase(get()) }
}

private val repositoryModule = module {
    single { RoleRepository() }
}
