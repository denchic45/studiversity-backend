package com.studiversity.feature.user

import com.studiversity.Constants
import com.studiversity.database.table.UserDao
import com.studiversity.database.table.toDomain
import com.studiversity.feature.scope.AddScopeRepoExt
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class UserRepository : AddScopeRepoExt {

    fun add(user: User) {
        transaction {
            UserDao.new(user.id) {
                firstName = user.firstName
                surname = user.firstName
                patronymic = user.patronymic
                email = user.email
            }
            addScope(user.id, 2, Constants.organizationId)
        }
    }

    fun findById(id: UUID): User? {
        return UserDao.findById(id)?.toDomain()
    }
}