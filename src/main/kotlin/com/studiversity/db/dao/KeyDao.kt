package com.studiversity.db.dao

import com.studiversity.db.table.Keys
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.select

class KeyDao {

    fun getJwtSecret() = Keys.select(where = { Keys.name eq "jwt_secret" }).map { it[Keys.name] }.single()
}