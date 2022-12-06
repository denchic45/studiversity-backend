package com.studiversity.database.table

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

class UserEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserEntity>(Users)

    var firstName by Users.firstName
    var surname by Users.surname
    var patronymic by Users.patronymic
    var email by Users.email
}

fun UserEntity.toDomain(): User = User(
    id = id.value,
    firstName = firstName,
    surname = surname,
    patronymic = patronymic,
    email = email
)