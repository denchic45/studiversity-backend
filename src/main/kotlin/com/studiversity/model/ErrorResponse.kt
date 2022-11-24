package com.studiversity.model

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
class ErrorResponse private constructor(val error: Error) {
    companion object {
        operator fun invoke(code: Int, error: String): ErrorResponse {
            return ErrorResponse(Error(code, error))
        }
    }
}

@Serializable
data class Error(val code: Int, val error: String)

suspend fun ApplicationCall.respondWithError(statusCode: HttpStatusCode, message: String) {
    respond(statusCode, ErrorResponse(statusCode.value, message))
}
