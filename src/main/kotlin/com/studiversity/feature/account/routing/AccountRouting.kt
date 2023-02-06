package com.studiversity.feature.account.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.accountRoutes() {
    routing {
        authenticate("auth-jwt") {
            personalRoute()
            securityRoute()
        }
    }
}