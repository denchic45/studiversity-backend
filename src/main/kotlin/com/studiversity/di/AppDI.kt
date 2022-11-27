package com.studiversity.di

import io.ktor.server.application.*
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun Application.appModule() = module {
    single(named("jwtAudience")) { this@appModule.environment.config.property("jwt.audience").getString() }
    single(named("jwtDomain")) {this@appModule.environment.config.property("jwt.domain").getString()  }
    single(named("jwtSecret")) { this@appModule.environment.config.property("jwt.secret").getString() }
}