package com.studiversity.feature.user

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.userRoutes() {
    routing {
        authenticate("auth-jwt") {
            route("/users") {
                userByIdRoute()
            }
        }
    }
}

private fun Route.userByIdRoute() {
    route("/{id}") {

    }
}


