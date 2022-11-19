package com.studiversity.db.dao

import com.studiversity.db.table.UserEntity
import com.studiversity.db.table.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UserDao {

    suspend fun insert(userEntity: UserEntity) = newSuspendedTransaction {
        Users.insert {
            it[firstName] = userEntity.firstName
            it[surname] = userEntity.surname
            it[patronymic] = userEntity.patronymic
            it[email] = userEntity.email
            it[password] = userEntity.password
        }
    }

    suspend fun getByLogin(login: String): UserEntity? = newSuspendedTransaction {
        Users.select { Users.email eq login }.map(::rowToEntity).firstOrNull()
    }

    suspend fun getAll(): List<UserEntity> = newSuspendedTransaction {
        Users.selectAll().map(::rowToEntity)
    }

    private fun rowToEntity(it: ResultRow) = UserEntity(
        id = it[Users.id], firstName = it[Users.firstName],
        surname = it[Users.surname],
        patronymic = it[Users.patronymic],
        email = it[Users.email],
        password = it[Users.password]
    )
}