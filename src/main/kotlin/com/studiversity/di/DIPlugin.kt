package com.studiversity.di

import com.studiversity.feature.auth.authModule
import com.studiversity.feature.course.di.courseModule
import com.studiversity.feature.course.subject.subjectModule
import com.studiversity.feature.role.di.roleModule
import com.studiversity.feature.studygroup.studyGroupModule
import com.studiversity.feature.user.userModule
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()
        modules(environmentModule, authModule, userModule, roleModule, studyGroupModule, courseModule, subjectModule)
    }
}