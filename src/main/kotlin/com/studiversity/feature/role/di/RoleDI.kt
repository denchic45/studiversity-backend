package com.studiversity.feature.role.di

import com.studiversity.feature.role.repository.RoleRepository
import com.studiversity.feature.role.usecase.*
import org.koin.dsl.module

private val useCaseModule = module {
    single { RequireCapabilityUseCase(get()) }
    single { RequireAvailableRolesInScopeUseCase(get()) }
    single { RequirePermissionToAssignRolesUseCase(get()) }
    single { FindRolesByNamesUseCase(get()) }
    single { FindAssignedUserRolesInScopeUseCase(get()) }
    single { UpdateUserRolesInScopeUseCase(get()) }
    single { AddUserToScopeUseCase(get()) }
    single { RemoveUserFromScopeUseCase(get()) }
    single { FindUsersInScopeUseCase(get()) }
}

private val repositoryModule = module {
    single { RoleRepository() }
}


val roleModule = module {
    includes(repositoryModule, useCaseModule)
}