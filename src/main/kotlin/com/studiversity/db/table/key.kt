package com.studiversity.db.table

import com.studiversity.db.table.Users.autoIncrement
import com.studiversity.util.varcharMax
import org.jetbrains.exposed.sql.Table

object Keys : Table("key") {
    val id = integer("id").autoIncrement()
    val name = varcharMax("name")
    val value = varcharMax("value")
}