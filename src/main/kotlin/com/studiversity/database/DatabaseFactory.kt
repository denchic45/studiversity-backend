package com.studiversity.database

import com.studiversity.DatabaseConstants
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    val database by lazy {
        Database.connect(
            url = DatabaseConstants.url,
            driver = DatabaseConstants.driver,
            user = DatabaseConstants.user,
            password = DatabaseConstants.password
        )
    }
}