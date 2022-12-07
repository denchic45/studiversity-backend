package com.studiversity.feature.role

import com.studiversity.database.exists
import com.studiversity.database.table.RolesCapabilities
import com.studiversity.database.table.ScopeEntity
import com.studiversity.database.table.UsersRolesScopes
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


class RoleRepository {

    fun hasRole(userId: UUID, roleId: Long, scopeId: UUID): Boolean = transaction {
        val scope = (ScopeEntity.findById(scopeId) ?: return@transaction false)
        val scopeIds = scope.path

        scopeIds.any { pathScopeId ->
            isExistRoleOfUserByScope(userId, roleId, UUID.fromString(pathScopeId))
        }
    }

    fun hasCapability(userId: UUID, capabilityResource: String, scopeId: UUID): Boolean = transaction {
        val path = ScopeEntity.findById(scopeId)!!.path
        for (nextScopeId in path) {
            return@transaction when (findCapabilityPermissionOfUserInScope(
                userId,
                UUID.fromString(nextScopeId),
                capabilityResource
            )) {
                Permission.Undefined -> continue
                Permission.Allow -> true
                Permission.Prohibit -> false
            }
        }
        return@transaction false
    }

    private fun findCapabilityPermissionOfUserInScope(
        userId: UUID,
        scopeId: UUID,
        capabilityResource: String
    ): Permission = UsersRolesScopes.slice(UsersRolesScopes.roleId)
        .select(
            UsersRolesScopes.userId eq userId and
                    (UsersRolesScopes.scopeId eq scopeId)
        ).map { usersRolesScopesRow ->
            val roleId = usersRolesScopesRow[UsersRolesScopes.roleId]
            RolesCapabilities.slice(RolesCapabilities.permission)
                .select(
                    RolesCapabilities.roleId eq roleId and
                            (RolesCapabilities.capabilityResource eq capabilityResource)
                ).map { rolesCapabilitiesRow -> rolesCapabilitiesRow[RolesCapabilities.permission] }
                .firstOrNull() ?: Permission.Undefined
        }.combinedPermission()

    private fun isExistRoleOfUserByScope(userId: UUID, roleId: Long, scopeId: UUID): Boolean {
        return UsersRolesScopes.exists {
            UsersRolesScopes.userId eq userId and
                    (UsersRolesScopes.roleId eq roleId) and
                    (UsersRolesScopes.scopeId eq scopeId)
        }
    }

    private fun checkRoleCapability(roleId: Long, capabilityResource: String, permission: Permission): Boolean {
        return RolesCapabilities.exists {
            RolesCapabilities.roleId eq roleId and
                    (RolesCapabilities.capabilityResource eq capabilityResource) and
                    (RolesCapabilities.permission eq permission)
        }
    }
}