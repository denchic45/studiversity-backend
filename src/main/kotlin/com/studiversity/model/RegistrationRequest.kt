package com.studiversity.model

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationRequest(
    val firstName: String,
    val surname: String,
    val patronymic: String,
    val email: String,
    val password: String
)
