package com.studiversity.database.dao

import com.studiversity.database.table.Users
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UserDao {

    suspend fun isEmailExist(email: String): Boolean = newSuspendedTransaction {
        !Users.select { Users.email eq email }.empty()
    }
}