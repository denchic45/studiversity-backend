package com.studiversity.feature.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.studiversity.feature.auth.model.LoginRequest
import com.studiversity.model.ErrorInfo
import com.studiversity.model.respondWithError
import com.studiversity.util.isEmail
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.routing.*
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject

fun Application.configureAuth() {

    val jwtSecret by inject<String>(named("jwtSecret"))
    val jwtAudience by inject<String>(named("jwtAudience"))

    authentication {
        jwt("auth-jwt") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
            challenge { defaultScheme, realm ->
                call.respondWithError(HttpStatusCode.Unauthorized, ErrorInfo("Token is not valid or has expired"))
            }
        }
    }

    routing {
        route("/auth") {
            install(RequestValidation) {
                validate<LoginRequest> { login ->
                    val password = login.password

                    buildList {
                        if (password.length < 6)
                            add(AuthErrors.PASSWORD_MUST_CONTAIN_AT_LEAST_6_CHARACTERS)

                        if (!password.contains("(?=.*[a-z])(?=.*[A-Z])".toRegex()))
                            add(AuthErrors.PASSWORD_MUST_CONTAIN_UPPER_AND_LOWER_CASE_CHARACTERS)

                        if (!password.contains("[0-9]".toRegex()))
                            add(AuthErrors.PASSWORD_MUST_CONTAIN_DIGITS)

                        if (!login.email.isEmail())
                            add(AuthErrors.WRONG_EMAIL)

                    }.let { errors ->
                        if (errors.isEmpty())
                            ValidationResult.Valid
                        else ValidationResult.Invalid(errors)
                    }
                }
            }
            signupRoute()
            tokenRoute()
        }
    }
}

object AuthErrors {
    const val REFRESH_TOKEN_REQUIRED = "REFRESH_TOKEN_REQUIRED"
    const val WRONG_REFRESH_TOKEN = "WRONG_REFRESH_TOKEN"
    const val WRONG_EMAIL = "WRONG_EMAIL"
    const val USER_ALREADY_REGISTERED = "USER_ALREADY_REGISTERED"
    const val PASSWORD_MUST_CONTAIN_AT_LEAST_6_CHARACTERS = "PASSWORD_MUST_CONTAIN_AT_LEAST_6_CHARACTERS"
    const val PASSWORD_MUST_CONTAIN_UPPER_AND_LOWER_CASE_CHARACTERS =
        "PASSWORD_MUST_CONTAIN_UPPER_AND_LOWER_CASE_CHARACTERS"
    const val PASSWORD_MUST_CONTAIN_DIGITS = "PASSWORD_MUST_CONTAIN_DIGITS"
}