package com.studiversity.db.table

import com.studiversity.util.varcharMax
import org.jetbrains.exposed.dao.id.UUIDTable

data class UserEntity(
    val firstName: String,
    val surname: String,
    val patronymic: String?,
    val email: String,
    val id: String
)

object Users : UUIDTable("user", "user_id") {
    val firstName = varcharMax("first_name")
    val surname = varcharMax("surname")
    val patronymic = varcharMax("patronymic").nullable()
    val email = varcharMax("email")
}