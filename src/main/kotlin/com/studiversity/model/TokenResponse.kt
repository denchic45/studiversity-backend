package com.studiversity.model

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val token: String,
    val refreshToken: String
)