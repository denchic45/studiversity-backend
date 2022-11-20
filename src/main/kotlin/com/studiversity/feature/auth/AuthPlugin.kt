package com.studiversity.feature.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject

fun Application.configureAuth() {

    val jwtSecret by inject<String>(named("jwtSecret"))

    authentication {
        jwt("auth-jwt") {
            val jwtAudience = this@configureAuth.environment.config.property("jwt.audience").getString()
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(this@configureAuth.environment.config.property("jwt.domain").getString())
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
    routing {
        loginRoute()
        registerRoute()
        tokenRoute()
    }
}
