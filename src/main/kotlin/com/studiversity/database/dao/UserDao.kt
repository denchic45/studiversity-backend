package com.studiversity.database.dao

import com.studiversity.database.table.UserEntity
import com.studiversity.database.table.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

class UserDao {

    suspend fun insert(userEntity: UserEntity) = newSuspendedTransaction {
        Users.insert {
            it[id] = UUID.fromString(userEntity.id)
            it[firstName] = userEntity.firstName
            it[surname] = userEntity.surname
            it[patronymic] = userEntity.patronymic
            it[email] = userEntity.email
        }
    }

    suspend fun getByLogin(login: String): UserEntity? = newSuspendedTransaction {
        Users.select { Users.email eq login }.map(::rowToEntity).firstOrNull()
    }

    suspend fun getAll(): List<UserEntity> = newSuspendedTransaction {
        Users.selectAll().map(::rowToEntity)
    }

    private fun rowToEntity(it: ResultRow) = UserEntity(
        id = it[Users.id].toString(),
        firstName = it[Users.firstName],
        surname = it[Users.surname],
        patronymic = it[Users.patronymic],
        email = it[Users.email]
    )

    suspend fun isEmailExist(email: String): Boolean = newSuspendedTransaction {
        !Users.select { Users.email eq email }.empty()
    }
}