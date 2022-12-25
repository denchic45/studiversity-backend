package com.studiversity.feature.membership

import com.studiversity.feature.membership.repository.MembershipRepository
import com.studiversity.feature.membership.repository.UserMembershipRepository
import org.koin.dsl.module

private val serviceModule = module {
    single { MembershipService(get(), get(), get(), get()) }
}

private val repositoryModule = module {
    single { MembershipRepository(get(), get()) }
    single { UserMembershipRepository(get()) }
}

val membershipModule = module {
    includes(serviceModule, repositoryModule)
}