package com.studiversity.plugins

import com.studiversity.db.dao.UserDao
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {

    routing {
        authenticate("auth-jwt") {
            get("/test") {
                call.respondText("Test completed!")
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
