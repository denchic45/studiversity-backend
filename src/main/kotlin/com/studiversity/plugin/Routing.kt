package com.studiversity.plugin

import com.studiversity.database.dao.UserDao
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureFeatures() {

    routing {
        authenticate("auth-jwt") {
            get("/test") {

                val principal = call.principal<JWTPrincipal>()
                val sub = principal!!.payload.getClaim("sub").asString()

                call.respondText("Test completed! $sub")
            }
            get("/fetch") {
                val userDao = UserDao()
                userDao.getAll().apply {
                    call.respond(this)
                }
            }
        }
    }
}
