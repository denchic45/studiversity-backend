package com.studiversity.model

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