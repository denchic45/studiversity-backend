package com.studiversity.feature.user

import org.koin.dsl.module

private val useCaseModule = module {

}

private val repositoryModule = module {
    single { UserRepository() }
}

val userModule = module {
    includes(useCaseModule, repositoryModule)
}