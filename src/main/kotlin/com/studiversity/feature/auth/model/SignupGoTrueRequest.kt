package com.studiversity.feature.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class SignupGoTrueRequest(
    val email: String,
    val password: String
)
