package com.studiversity.database.table

import com.studiversity.util.varcharMax
import org.jetbrains.exposed.dao.id.UUIDTable

object Memberships : UUIDTable("membership", "membership_id") {
    val scopeId = reference("scope_id", Scopes.id)
    val active = bool("active")
    val type = varcharMax("type")
}