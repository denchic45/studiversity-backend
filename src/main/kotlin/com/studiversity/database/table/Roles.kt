package com.studiversity.database.table

import com.studiversity.util.varcharMax
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object Roles : LongIdTable("role", "role_id") {
    val name = varcharMax("role_name")
    val shortName = varcharMax("short_name")
}

class RoleDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<RoleDao>(Roles) {

        fun findIdByName(shortName: String): Long {
            return find(Roles.shortName eq shortName).first().id.value
        }
    }

    var name by Roles.name
    var shortName by Roles.shortName
}