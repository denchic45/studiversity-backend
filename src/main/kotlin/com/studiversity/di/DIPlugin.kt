package com.studiversity.di

import com.studiversity.model.ErrorInfo
import com.studiversity.model.respondWithError
import com.studiversity.model.respondWithErrors
import com.studiversity.model.toErrors
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDI() {
    // Install Ktor features
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
        exception<RequestValidationException> { call, cause ->
            call.respondWithErrors(HttpStatusCode.BadRequest, cause.reasons.toErrors())
        }

    }
    install(Koin) {
        slf4jLogger()
        modules(appModule(), authModule, repositoryModule, useCaseModule)
    }
}