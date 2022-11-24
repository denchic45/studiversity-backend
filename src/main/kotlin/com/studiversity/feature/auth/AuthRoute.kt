package com.studiversity.feature.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.studiversity.db.dao.UserDao
import com.studiversity.db.table.UserEntity
import com.studiversity.model.LoginRequest
import com.studiversity.model.RefreshTokenResponse
import com.studiversity.model.RegistrationRequest
import com.studiversity.model.respondWithError
import com.studiversity.util.isEmail
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.providers.builtin.Email
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
    val goTrue by inject<GoTrue>()

    post("/login") {
        goTrue.loginWith(Email) {

        }
        val loginRequest = call.receive<LoginRequest>()
        userDao.getByLogin(loginRequest.email)?.let { userEntity ->
            if (!BCrypt.checkpw(loginRequest.password, userEntity.password)) {
                return@post call.respondWithError(HttpStatusCode.Forbidden, "INVALID_PASSWORD")
            }
            val token = createToken(userEntity.id, audience, jwtSecret, domain)
            call.respond(hashMapOf("token" to token))
        } ?: call.respondWithError(HttpStatusCode.BadRequest, "EMAIL_NOT_FOUND")

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
                return@post call.respondWithError(HttpStatusCode.Unauthorized, "WRONG_EMAIL")
            if (userDao.isEmailExist(registrationRequest.email))
                return@post call.respondWithError(HttpStatusCode.Unauthorized, "EMAIL_EXIST")

            registrationRequest.password.apply {
                if (this.length < 6)
                    return@post call.respondWithError(
                        HttpStatusCode.Unauthorized,
                        "PASSWORD_MUST_CONTAIN_AT_LEAST_6_CHARACTERS"
                    )
                if (!this.contains("(?=.*[a-z])(?=.*[A-Z])".toRegex()))
                    return@post call.respondWithError(
                        HttpStatusCode.Unauthorized,
                        "PASSWORD_MUST_CONTAIN_UPPER_AND_LOWER_CASE_CHARACTERS"
                    )
                if (!this.contains("[0-9]".toRegex()))
                    return@post call.respondWithError(HttpStatusCode.Unauthorized, "PASSWORD_MUST_CONTAIN_DIGITS")
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
            call.respondWithError(HttpStatusCode.BadRequest, "NOT_CORRECT_FIELDS")
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
        } else
            return@post call.respondWithError(HttpStatusCode.BadRequest, "WRONG_REFRESH_TOKEN")

    }
}