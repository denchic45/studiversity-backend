package com.studiversity.di

import com.studiversity.feature.auth.authModule
import com.studiversity.feature.course.courseModule
import com.studiversity.feature.membership.membershipModule
import com.studiversity.feature.role.roleModule
import com.studiversity.feature.studygroup.studyGroupModule
import com.studiversity.feature.user.userModule
import com.studiversity.transaction.DatabaseTransactionWorker
import com.studiversity.transaction.SuspendTransactionWorker
import com.studiversity.transaction.TransactionWorker
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.binds
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

val coroutineModule = module { single { CoroutineScope(SupervisorJob()) } }

val transactionModule = module {
    factory { DatabaseTransactionWorker() } binds arrayOf(TransactionWorker::class, SuspendTransactionWorker::class)
}

fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()
        modules(
            coroutineModule,
            transactionModule,
            environmentModule,
            supabaseClientModule,
            authModule,
            userModule,
            roleModule,
            membershipModule,
            studyGroupModule,
            courseModule,
        )
    }
}