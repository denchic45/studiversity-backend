package com.studiversity.database.table

import com.studiversity.database.exists
import com.studiversity.feature.user.User
import com.studiversity.util.varcharMax
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*


object Users : UUIDTable("user", "user_id") {
    val firstName = varcharMax("first_name")
    val surname = varcharMax("surname")
    val patronymic = varcharMax("patronymic").nullable()
    val email = varcharMax("email")
}

class UserDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserDao>(Users) {
        fun isExistEmail(email: String): Boolean {
            return table.exists { Users.email eq email }
        }
    }

    var firstName by Users.firstName
    var surname by Users.surname
    var patronymic by Users.patronymic
    var email by Users.email
}

fun UserDao.toDomain(): User = User(
    id = id.value,
    firstName = firstName,
    surname = surname,
    patronymic = patronymic,
    email = email
)