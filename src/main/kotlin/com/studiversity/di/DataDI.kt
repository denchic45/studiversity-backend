package com.studiversity.di

import com.studiversity.feature.group.StudyGroupRepository
import com.studiversity.feature.role.RoleRepository
import com.studiversity.feature.user.UserRepository
import org.koin.dsl.module

val repositoryModule = module {
    single { RoleRepository() }
    single { UserRepository() }
    single { StudyGroupRepository() }
}