package com.studiversity.di

import io.ktor.server.application.*
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun Application.appModule() = module {
    single(named("audience")) { this@appModule.environment.config.property("jwt.audience").getString() }
    single(named("domain")) {this@appModule.environment.config.property("jwt.domain").getString()  }
    single(named("jwtSecret")) { this@appModule.environment.config.property("jwt.secret").getString() }
}