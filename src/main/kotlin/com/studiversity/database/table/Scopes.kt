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
    val type: Column<EntityID<Long>> = reference("type", ScopeTypes.id)
}

class ScopeEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<ScopeEntity>(Scopes)

    var path: List<String> by Scopes.path.transform(
        toColumn = { it.reversed().joinToString("/") },
        toReal = { it.split("/").reversed() }
    )
    var type by ScopeTypeEntity referencedOn Scopes.type
}