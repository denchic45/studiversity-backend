package com.studiversity.feature.membership

import com.studiversity.feature.membership.controller.ManualMembershipController
import com.studiversity.feature.membership.repository.MembershipRepository
import com.studiversity.feature.membership.repository.UserMembershipRepository
import com.studiversity.feature.membership.usecase.AddUserToMembershipUseCase
import com.studiversity.feature.membership.usecase.RemoveUserFromMembershipUseCase
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val membershipControllerModule = module {
    factory(named("manual")) { parameters ->
        ManualMembershipController(
            parameters.get(),
            parameters.get(),
            parameters.get()
        )
    }
}

private val useCaseModule = module {
    single { AddUserToMembershipUseCase(get(), get()) }
    single { RemoveUserFromMembershipUseCase(get(), get()) }
}

private val serviceModule = module {
    single { MembershipService(get(), get(), get(), get()) }
}

private val repositoryModule = module {
    single { MembershipRepository(get(), get()) }
    single { UserMembershipRepository(get()) }
}

val membershipModule = module {
    includes(serviceModule, useCaseModule, repositoryModule)
}