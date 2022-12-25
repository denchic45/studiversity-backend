package com.studiversity.di

import com.studiversity.feature.auth.authModule
import com.studiversity.feature.course.di.courseModule
import com.studiversity.feature.course.subject.subjectModule
import com.studiversity.feature.membership.membershipModule
import com.studiversity.feature.role.di.roleModule
import com.studiversity.feature.studygroup.studyGroupModule
import com.studiversity.feature.user.userModule
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

val coroutineModule = module { single { CoroutineScope(SupervisorJob()) } }

fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()
        modules(
            coroutineModule,
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