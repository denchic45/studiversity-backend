package com.studiversity.supabase.model

import com.studiversity.feature.auth.AuthErrors
import com.studiversity.model.ErrorInfo
import com.studiversity.model.SingleErrorResponse
import com.studiversity.model.respondWithError
import com.studiversity.util.bodyOrNull
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupabaseErrorResponse(val code: Int, val msg: String)

@Serializable
data class SupabaseError2Response(
    val error: String,
    @SerialName("error_description") val errorDescription: String
)

private const val SUPABASE_ERROR = "SUPABASE_ERROR"

suspend fun ApplicationCall.respondWithSupabaseError(response: HttpResponse) = response.apply {
    val error = bodyOrNull<SupabaseErrorResponse>()?.mapResponse()
        ?: bodyOrNull<SupabaseError2Response>()?.mapResponse()
        ?: SingleErrorResponse(
            HttpStatusCode.InternalServerError.value,
            ErrorInfo(SUPABASE_ERROR)
        )
    respondWithError(error)
}

fun SupabaseErrorResponse.mapResponse(): SingleErrorResponse = SingleErrorResponse(
    code = code,
    error = msgToErrorInfo.getOrDefault(
        msg,
        ErrorInfo("")
    )
)

fun SupabaseError2Response.mapResponse(): SingleErrorResponse = SingleErrorResponse(
    code = HttpStatusCode.BadRequest.value,
    error = errorToErrorInfo.getOrDefault(
        error,
        ErrorInfo(SUPABASE_ERROR)
    )
)

private val msgToErrorInfo = mapOf(
    "User.kt already registered" to ErrorInfo(AuthErrors.USER_ALREADY_REGISTERED)
)

private val errorToErrorInfo = mapOf(
    "invalid_request" to ErrorInfo(AuthErrors.REFRESH_TOKEN_REQUIRED),
    "invalid_grant" to ErrorInfo(AuthErrors.WRONG_REFRESH_TOKEN)
)