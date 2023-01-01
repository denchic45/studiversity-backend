package com.studiversity.plugin

import com.studiversity.ktor.ForbiddenException
import com.studiversity.model.ErrorInfo
import com.studiversity.model.respondWithError
import com.studiversity.model.respondWithErrors
import com.studiversity.model.toErrors
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<BadRequestException> { call, exception ->
            call.respondWithError(
                HttpStatusCode.BadRequest,
                ErrorInfo(
                    if (exception.message == "Illegal input") {
                        "INVALID_INPUT"
                    } else exception.message ?: ""
                )
            )
        }
        exception<NotFoundException> { call, exception ->
            call.respondWithError(HttpStatusCode.NotFound, ErrorInfo(exception.message ?: ""))
        }
        exception<ForbiddenException> { call, exception ->
            call.respondWithError(HttpStatusCode.Forbidden, ErrorInfo(exception.message ?: ""))
        }
        exception<RequestValidationException> { call, cause ->
            call.respondWithErrors(HttpStatusCode.BadRequest, cause.reasons.toErrors())
        }
    }
}