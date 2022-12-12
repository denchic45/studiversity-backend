package com.studiversity.feature.user

import org.koin.dsl.module

val userModule = module {
    includes(useCaseModule, repositoryModule)
}

private val useCaseModule = module {

}

private val repositoryModule = module {
    single { UserRepository() }
}