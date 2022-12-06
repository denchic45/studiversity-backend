package com.studiversity.database.table

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object UsersRolesScopes : LongIdTable("user_role_scope", "user_role_scope_id") {
    val userId = uuid("user_id").references(Users.id)
    val roleId = long("role_id").references(Roles.id)
    val scopeId = uuid("scope_id").references(Scopes.id)
}

class UserRoleScopeEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserRoleScopeEntity>(UsersRolesScopes)

}