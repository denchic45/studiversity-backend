package com.studiversity.di

import io.ktor.server.application.*
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val Application.environmentModule: Module
    get() = module {
        single(named("jwtAudience")) { this@environmentModule.environment.config.property("jwt.audience").getString() }
        single(named("jwtDomain")) { this@environmentModule.environment.config.property("jwt.domain").getString() }
        single(named("jwtSecret")) { this@environmentModule.environment.config.property("jwt.secret").getString() }
    }