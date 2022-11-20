package com.studiversity.feature.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.studiversity.db.dao.UserDao
import com.studiversity.db.table.UserEntity
import com.studiversity.model.ErrorResponse
import com.studiversity.model.LoginRequest
import com.studiversity.model.RefreshTokenResponse
import com.studiversity.model.RegistrationRequest
import com.studiversity.util.isEmail
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject
import org.mindrot.jbcrypt.BCrypt
import java.util.*

fun Route.loginRoute() {
    val userDao by inject<UserDao>()
    val audience by inject<String>(named("audience"))
    val domain by inject<String>(named("domain"))
    val jwtSecret by inject<String>(named("jwtSecret"))

    post("/login") {
        val loginRequest = call.receive<LoginRequest>()
        userDao.getByLogin(loginRequest.email)?.let { userEntity ->
            if (BCrypt.checkpw(loginRequest.password, userEntity.password)) {
                val token = createToken(userEntity.id, audience, jwtSecret, domain)
                call.respond(
                    hashMapOf("token" to token)
                )
            } else {
                call.respond(HttpStatusCode.Forbidden, "Not correct password")
            }
        } ?: call.respond(HttpStatusCode.BadRequest, "Not correct email")

    }
}

private fun createToken(
    userId: String,
    audience: String,
    jwtSecret: String,
    domain: String
): String = JWT.create()
    .withAudience(audience)
    .withIssuer(domain)
    .withClaim("userId", userId)
    .withExpiresAt(Date(System.currentTimeMillis() + 60000))
    .sign(Algorithm.HMAC256(jwtSecret))

fun Route.registerRoute() {
    val userDao by inject<UserDao>()
    post("/register") {
        val registrationRequest = call.receiveNullable<RegistrationRequest>()
        if (registrationRequest != null) {

            if (!registrationRequest.email.isEmail())
                return@post call.respond(HttpStatusCode.Unauthorized, "Wrong email")
            if (userDao.isEmailExist(registrationRequest.email))
                return@post call.respond(HttpStatusCode.Unauthorized, "This email address is already taken")

            registrationRequest.password.apply {
                if (this.length < 6)
                    return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        "Password must contain at least 6 characters"
                    )
                if (!this.contains("(?=.*[a-z])(?=.*[A-Z])".toRegex()))
                    return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        "Password must contain upper and lower case characters"
                    )
                if (!this.contains("[0-9]".toRegex()))
                    return@post call.respond(HttpStatusCode.Unauthorized, "Password must contain numbers")
            }

            val hashed: String = BCrypt.hashpw(registrationRequest.password, BCrypt.gensalt())
            val refreshToken = UUID.randomUUID().toString()

            userDao.insert(
                UserEntity(
                    firstName = registrationRequest.firstName,
                    surname = registrationRequest.surname,
                    patronymic = registrationRequest.patronymic,
                    email = registrationRequest.email,
                    password = hashed,
                    refreshToken = refreshToken
                )
            )
            call.respond("User ${registrationRequest.firstName} successfully registered!")
        } else {
            call.respond("Not correct fields!")
        }
    }
}

fun Route.tokenRoute() {
    val userDao by inject<UserDao>()
    val audience by inject<String>(named("audience"))
    val domain by inject<String>(named("domain"))
    val jwtSecret by inject<String>(named("jwtSecret"))
    post("/token") {
        val refreshToken = call.parameters.getOrFail("refresh_token")
        val userId = call.parameters.getOrFail("user_id")

        val actualRefreshToken = userDao.getRefreshToken(UUID.fromString(userId))

        if (refreshToken == actualRefreshToken) {
            val newRefreshToken = UUID.randomUUID().toString()
            userDao.updateRefreshToken(
                userId = UUID.fromString(userId),
                newRefreshToken = newRefreshToken
            )
            call.respond(
                RefreshTokenResponse(
                    createToken(userId, audience, jwtSecret, domain),
                    newRefreshToken,
                    userId
                )
            )
        } else return@post call.respond(
            HttpStatusCode.BadRequest,
            ErrorResponse(HttpStatusCode.BadRequest.value, "WRONG_REFRESH_TOKEN")
        )

    }
}