package com.stuiversity.util

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
