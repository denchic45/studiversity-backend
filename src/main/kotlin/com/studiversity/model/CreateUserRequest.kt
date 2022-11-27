package com.studiversity.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val firstName: String,
    val surname: String,
    val patronymic: String? = null,
    val roles: List<String> = listOf(),
    val email: String,
    val password: String
)