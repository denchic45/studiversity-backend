package com.studiversity.feature.user.account.routing

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.studiversity.feature.auth.addPasswordConditions
import com.studiversity.feature.user.account.usecase.UpdateEmailUseCase
import com.studiversity.feature.user.account.usecase.UpdatePasswordUseCase
import com.studiversity.ktor.currentUserId
import com.studiversity.util.isEmail
import com.studiversity.util.respondWithError
import com.studiversity.validation.buildValidationResult
import com.stuiversity.api.account.model.UpdateEmailRequest
import com.stuiversity.api.account.model.UpdatePasswordRequest
import com.stuiversity.api.auth.AuthErrors
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.securityRoute() {
    val updatePassword: UpdatePasswordUseCase by inject()
    val updateEmail: UpdateEmailUseCase by inject()
    install(RequestValidation) {
        validate<UpdatePasswordRequest> { request ->
            buildValidationResult {
                addPasswordConditions(request.oldPassword)
            }
        }
        validate<UpdateEmailRequest> { request ->
            buildValidationResult {
                condition(request.email.isEmail(), AuthErrors.WRONG_EMAIL)
            }
        }
    }
    post("/password") {
        when (val response = updatePassword(call.currentUserId(), call.receive())) {
            is Ok -> call.respond(HttpStatusCode.OK)
            is Err -> call.respondWithError(response.error)
        }
    }
    post("/email") {
        when (val response = updateEmail(call.currentUserId(), call.receive())) {
            is Ok -> call.respond(HttpStatusCode.OK)
            is Err -> call.respondWithError(response.error)
        }
    }
}