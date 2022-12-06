package com.studiversity.database.table

import com.studiversity.util.varcharMax
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object ScopeTypes : LongIdTable("scope_type", "scope_type_id") {
    val name = varcharMax("scope_type_name")
    val parent = reference("parent_scope", ScopeTypes)
}

class ScopeTypeEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ScopeTypeEntity>(ScopeTypes)
}