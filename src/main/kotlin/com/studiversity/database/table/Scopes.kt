package com.studiversity.database.table

import com.studiversity.util.varcharMax
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import java.util.*

object Scopes : UUIDTable("scope", "instance_id") {
    val path = varcharMax("path")
    val type: Column<EntityID<Long>> = reference("type", ScopeTypes)
}

class ScopeEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ScopeEntity>(Scopes) {
        fun newScope(id: UUID, type: Long) {
            
        }
    }

    var path by Scopes.path
    var type by ScopeTypeEntity referencedOn Scopes.type
}