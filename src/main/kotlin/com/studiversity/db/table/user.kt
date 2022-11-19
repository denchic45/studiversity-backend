package com.studiversity.db.table

import com.studiversity.util.varcharMax
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class UserEntity(
    val id: Int = -1,
    val firstName: String,
    val surname: String,
    val patronymic: String,
    val email: String,
    val password: String
)

object Users : Table("user") {
    val id = integer("id").autoIncrement()
    val firstName = varcharMax("first_name")
    val surname = varcharMax("surname")
    val patronymic = varcharMax("patronymic")
    val email = varcharMax("email")
    val password = varcharMax("password")

    override val primaryKey = PrimaryKey(id)
}