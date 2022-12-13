package com.studiversity.feature.role.repository

import com.studiversity.database.exists
import com.studiversity.database.table.*
import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.Permission
import com.studiversity.feature.role.Role
import com.studiversity.feature.role.combinedPermission
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


class RoleRepository {

    fun hasRole(userId: UUID, roleId: Long, scopeId: UUID): Boolean = transaction {
        val scope = (ScopeDao.findById(scopeId) ?: return@transaction false)
        val scopeIds = scope.path

        scopeIds.any { pathScopeId ->
            isExistRoleOfUserByScope(userId, roleId, pathScopeId)
        }
    }

    private fun isExistRoleOfUserByScope(userId: UUID, roleId: Long, scopeId: UUID): Boolean {
        return UsersRolesScopes.exists {
            UsersRolesScopes.userId eq userId and
                    (UsersRolesScopes.roleId eq roleId) and
                    (UsersRolesScopes.scopeId eq scopeId)
        }
    }

    private fun getUserRolesByScope(userId: UUID, scopeId: UUID): List<Long> {
        return UserRoleScopeDao.find(
            UsersRolesScopes.userId eq userId and (UsersRolesScopes.scopeId eq scopeId)
        ).map { it.roleId }
    }

    fun hasCapability(userId: UUID, capability: Capability, scopeId: UUID): Boolean = transaction {
        val path = ScopeDao.findById(scopeId)!!.path
        for (nextScopeId in path) {
            return@transaction when (findCapabilityPermissionOfUserInScope(
                userId,
                nextScopeId,
                capability.toString()
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
            UsersRolesScopes.userId eq userId
                    and (UsersRolesScopes.scopeId eq scopeId)
        ).map { usersRolesScopesRow ->
            val roleId = usersRolesScopesRow[UsersRolesScopes.roleId]
            RolesCapabilities.slice(RolesCapabilities.permission)
                .select(
                    RolesCapabilities.roleId eq roleId
                            and (RolesCapabilities.capabilityResource eq capabilityResource)
                ).map { rolesCapabilitiesRow -> rolesCapabilitiesRow[RolesCapabilities.permission] }
                .firstOrNull() ?: Permission.Undefined
        }.combinedPermission()

    private fun checkRoleCapability(roleId: Long, capabilityResource: String, permission: Permission): Boolean {
        return RolesCapabilities.exists {
            RolesCapabilities.roleId eq roleId and
                    (RolesCapabilities.capabilityResource eq capabilityResource) and
                    (RolesCapabilities.permission eq permission)
        }
    }

    fun existRolesByScope(roles: List<Role>, scopeId: UUID): Boolean = transaction {
        roles.all { role ->
            val scopeType = ScopeDao.findById(scopeId)!!.scopeTypeId.value
            RolesScopes.exists { RolesScopes.roleId eq role.id and (RolesScopes.scopeId eq scopeType) }
        }
    }

    fun existPermissionRolesByUserId(userId: UUID, assignRoles: List<Role>, scopeId: UUID) = transaction {
        assignRoles.all { assignRole ->
            existPermissionRoleByUserId(userId, assignRole, scopeId)
        }
    }

    private fun existPermissionRoleByUserId(userId: UUID, assignRole: Role, scopeId: UUID): Boolean {
        return ScopeDao.findById(scopeId)!!.path.all { segmentScopeId ->
            getUserRolesByScope(userId, segmentScopeId).all { role ->
                existRoleAssignment(role, RoleDao.findIdByName(assignRole.resource))
            }
        }
    }


    private fun existRoleAssignment(roleId: Long, assignRoleId: Long): Boolean {
        return RolesAssignments.exists {
            RolesAssignments.roleId eq roleId and (RolesAssignments.assignRoleId eq assignRoleId)
        }
    }

    fun findByNames(roleNames: List<String>) = transaction {
        roleNames.map { name ->
            RoleDao.find(Roles.shortName eq name)
                .singleOrNull()?.let { Role(it.id.value, it.shortName) }
        }
    }

    fun existUserByScope(userId: UUID, scopeId: UUID) = transaction {
        UsersRolesScopes.exists { UsersRolesScopes.userId eq userId and (UsersRolesScopes.scopeId eq scopeId) }
    }
}