package com.studiversity.di

import com.studiversity.feature.auth.authModule
import com.studiversity.feature.course.di.courseModule
import com.studiversity.feature.course.subject.subjectModule
import com.studiversity.feature.membership.membershipModule
import com.studiversity.feature.role.roleModule
import com.studiversity.feature.studygroup.studyGroupModule
import com.studiversity.feature.user.userModule
import com.studiversity.transaction.DatabaseTransactionWorker
import com.studiversity.transaction.TransactionWorker
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

val coroutineModule = module { single { CoroutineScope(SupervisorJob()) } }

val transactionModule = module { factory { DatabaseTransactionWorker() } bind TransactionWorker::class }

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
            subjectModule
        )
    }
}