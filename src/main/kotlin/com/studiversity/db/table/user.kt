package com.studiversity.db.table

import com.studiversity.util.varcharMax
import org.jetbrains.exposed.dao.id.UUIDTable

data class UserEntity(
    val id: String = "",
    val firstName: String,
    val surname: String,
    val patronymic: String,
    val email: String,
    val password: String,
    val refreshToken: String
)

object Users : UUIDTable("user") {
    val firstName = varcharMax("first_name")
    val surname = varcharMax("surname")
    val patronymic = varcharMax("patronymic")
    val email = varcharMax("email")
    val password = varcharMax("password")
    val refreshToken = varcharMax("refresh_token")
}