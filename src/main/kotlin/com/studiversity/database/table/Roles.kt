package com.studiversity.database.table

import com.studiversity.util.varcharMax
import org.jetbrains.exposed.dao.id.LongIdTable

object Roles : LongIdTable("role", "role_id") {
    val name = varcharMax("role_name")
}