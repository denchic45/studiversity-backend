package com.studiversity.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.studiversity.db.dao.UserDao
import com.studiversity.db.table.UserEntity
import com.studiversity.model.LoginRequest
import com.studiversity.model.RegistrationRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

fun Application.configureSecurity() {

    val jwtSecret = this@configureSecurity.environment.config.property("jwt.secret").getString()

    val userDao = UserDao()
    authentication {
        jwt("auth-jwt") {
            val jwtAudience = this@configureSecurity.environment.config.property("jwt.audience").getString()
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(this@configureSecurity.environment.config.property("jwt.domain").getString())
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
        post("/register") {
            val registration = call.receiveNullable<RegistrationRequest>()
            if (registration != null) {
                val hashed: String = BCrypt.hashpw(registration.password, BCrypt.gensalt())
                userDao.insert(
                    UserEntity(
                        firstName = registration.firstName,
                        surname = registration.surname,
                        patronymic = registration.patronymic,
                        email = registration.email,
                        password = hashed
                    )
                )
                call.respond("User ${registration.firstName} successfully registered!")
            } else {
                call.respond("Not correct fields!")
            }
        }
        post("/login") {
            val loginRequest = call.receive<LoginRequest>()
            userDao.getByLogin(loginRequest.email)?.let { userEntity ->
                if (BCrypt.checkpw(loginRequest.password, userEntity.password)) {
                    val audience = this@configureSecurity.environment.config.property("jwt.audience").getString()
                    val date = System.currentTimeMillis() + 60000
                    val token = JWT.create()
                        .withAudience(audience)
                        .withIssuer(this@configureSecurity.environment.config.property("jwt.domain").getString())
                        .withClaim("email", loginRequest.email)
                        .withClaim("password", loginRequest.password)
                        .withExpiresAt(Date(date))
                        .sign(Algorithm.HMAC256(jwtSecret))
                    call.respond(
                        hashMapOf(
                            "date" to DateTimeFormatter.ISO_DATE_TIME.format(
                                LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(date),
                                    ZoneId.systemDefault()
                                )
                            ), "token" to token
                        )
                    )
                } else {
                    call.respond(HttpStatusCode.Forbidden, "Not correct password")
                }
            } ?: call.respond(HttpStatusCode.BadRequest, "Not correct email")

        }
    }
}
