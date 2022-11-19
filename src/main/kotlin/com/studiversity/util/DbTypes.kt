package com.studiversity.util

import com.studiversity.db.table.Users.registerColumn
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.VarCharColumnType

fun varcharMax(name: String, collate: String? = null): Column<String> =
    registerColumn(name, VarCharColumnType(collate = collate))