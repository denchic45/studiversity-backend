package com.studiversity.feature.role

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.configureRoles() {
    routing {
        authenticate("auth-jwt") {
            userAssignedRolesRoute()
        }
    }
}