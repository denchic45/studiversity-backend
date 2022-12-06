package com.studiversity.feature.role

import com.studiversity.database.exists
import com.studiversity.database.table.ScopeEntity
import com.studiversity.database.table.UsersRolesScopes
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


class RoleRepository {

    fun isExistUserRoleByScopeOrParentScopes(userId: UUID, roleId: Long, scopeId: UUID): Boolean = transaction {

        val scope = (ScopeEntity.findById(scopeId) ?: return@transaction false)
        val scopeIds = scope.path.split("/").reversed()

        scopeIds.any { pathScopeId ->
            isExistUserRoleByScope(userId, roleId, UUID.fromString(pathScopeId))
        }
    }

    private fun isExistUserRoleByScope(userId: UUID, roleId: Long, scopeId: UUID): Boolean {
        return UsersRolesScopes.exists {
            UsersRolesScopes.userId eq userId and
                    (UsersRolesScopes.roleId eq roleId) and
                    (UsersRolesScopes.scopeId eq scopeId)
        }
    }
}