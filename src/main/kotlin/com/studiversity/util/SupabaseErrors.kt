package com.studiversity.util

import com.studiversity.supabase.model.SupabaseErrorResponse
import com.stuiversity.util.ErrorInfo
import com.stuiversity.util.ErrorResponse
import com.stuiversity.util.ErrorsResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

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