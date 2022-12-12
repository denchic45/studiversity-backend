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

class UserRoleScopeDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<UserRoleScopeDao>(UsersRolesScopes)

    var userId by UsersRolesScopes.userId
    var roleId by UsersRolesScopes.roleId
    var scopeId by UsersRolesScopes.scopeId

    var role by RoleDao referencedOn UsersRolesScopes.roleId

}