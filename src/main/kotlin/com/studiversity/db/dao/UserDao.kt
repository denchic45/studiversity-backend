package com.studiversity.db.dao

import com.studiversity.db.table.UserEntity
import com.studiversity.db.table.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

class UserDao {

    suspend fun insert(userEntity: UserEntity) = newSuspendedTransaction {
        Users.insert {
            it[firstName] = userEntity.firstName
            it[surname] = userEntity.surname
            it[patronymic] = userEntity.patronymic
            it[email] = userEntity.email
            it[password] = userEntity.password
            it[refreshToken] = userEntity.refreshToken
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
        email = it[Users.email],
        password = it[Users.password],
        refreshToken = it[Users.refreshToken]
    )

    suspend fun isEmailExist(email: String): Boolean = newSuspendedTransaction {
        !Users.select { Users.email eq email }.empty()
    }

    suspend fun getRefreshToken(userId: UUID) = newSuspendedTransaction {
        Users.select { Users.id eq userId }.map { it[Users.refreshToken].toString() }.single()
    }

    suspend fun updateRefreshToken(userId: UUID, newRefreshToken: String) = newSuspendedTransaction {
        Users.update({ Users.id eq userId }) { it[refreshToken] = newRefreshToken }
    }
}