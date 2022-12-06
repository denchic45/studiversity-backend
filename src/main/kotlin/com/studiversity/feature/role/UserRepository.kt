package com.studiversity.feature.role

import com.studiversity.database.dao.UserDao
import com.studiversity.database.table.ScopeEntity
import com.studiversity.database.table.UserEntity
import com.studiversity.database.table.toDomain
import com.studiversity.feature.user.User
import java.util.*

class UserRepository(private val userDao: UserDao) {

    fun add(user: User) {
        UserEntity.new(user.id) {
            firstName = user.firstName
            surname = user.firstName
            patronymic = user.patronymic
            email = user.email
        }

        ScopeEntity.new(user.id) {}
    }

    fun findById(id: UUID): User? {
        return UserEntity.findById(id)?.toDomain()
    }
}