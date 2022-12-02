package com.studiversity.feature.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val firstName: String,
    val surname: String,
    val patronymic: String? = null,
    val systemRoles: List<String> = listOf(),
    val email: String,
    val password: String
)