package com.studiversity.model

import com.studiversity.supabase.model.SupabaseErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
class ErrorResponse(val code: Int, val error: ErrorInfo)

@Serializable
class ErrorsResponse(val code: Int, val errors: List<ErrorInfo>)

fun List<String>.toErrors(): List<ErrorInfo> {
    return map { ErrorInfo(it) }
}

@Serializable
data class ErrorInfo(val reason: String)

//@Serializable
//sealed class ErrorWrapper {
//    @Serializable
//    data class SingleError(val reason: String) : ErrorWrapper()
//
//    @Serializable
//    data class Errors(val errors: List<SingleError>) : ErrorWrapper()
//}

suspend fun ApplicationCall.respondWithError(errorResponse: ErrorResponse) {
    respond(
        HttpStatusCode.fromValue(errorResponse.code),
        errorResponse
    )
}

suspend fun ApplicationCall.respondWithError(statusCode: HttpStatusCode, error: ErrorInfo) {
    respond(statusCode, ErrorResponse(statusCode.value, error))
}

suspend fun ApplicationCall.respondWithErrors(statusCode: HttpStatusCode, errors: List<ErrorInfo>) {
    respond(statusCode, ErrorsResponse(statusCode.value, errors))
}

suspend fun ApplicationCall.respondWithError(supabaseErrorResponse: SupabaseErrorResponse) {
    respond(
        HttpStatusCode.fromValue(supabaseErrorResponse.code),
        ErrorResponse(supabaseErrorResponse.code, ErrorInfo(supabaseErrorResponse.msg))
    )
}