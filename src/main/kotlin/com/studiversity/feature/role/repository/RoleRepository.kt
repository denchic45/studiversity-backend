package com.studiversity.feature.role.repository

import com.studiversity.database.exists
import com.studiversity.database.table.*
import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.Permission
import com.studiversity.feature.role.Role
import com.studiversity.feature.role.combinedPermission
import com.studiversity.feature.role.mapper.toUserRolesResponse
import com.studiversity.feature.role.mapper.toUsersWithRoles
import com.studiversity.feature.role.model.UpdateUserRolesRequest
import com.studiversity.feature.role.model.UserRolesResponse
import com.studiversity.feature.role.model.UserWithRolesResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


class RoleRepository {

    fun hasRole(userId: UUID, role: Role, scopeId: UUID, checkParentScopes: Boolean = true): Boolean = transaction {
        val scope = (ScopeDao.findById(scopeId) ?: return@transaction false)

        if (!checkParentScopes)
            return@transaction isExistRoleOfUserByScope(userId, role.id, scopeId)

        val scopeIds = scope.path
        scopeIds.any { pathScopeId -> isExistRoleOfUserByScope(userId, role.id, pathScopeId) }
    }

    fun hasRoleIn(userId: UUID, role: Role, scopeIds: List<UUID>, checkParentScopes: Boolean = true): Boolean {
        return transaction {
            scopeIds.any { hasRole(userId, role, it, checkParentScopes) }
        }
    }

    private fun isExistRoleOfUserByScope(userId: UUID, roleId: Long, scopeId: UUID): Boolean {
        return UsersRolesScopes.exists {
            UsersRolesScopes.userId eq userId and
                    (UsersRolesScopes.roleId eq roleId) and
                    (UsersRolesScopes.scopeId eq scopeId)
        }
    }

    private fun getUserRoleIdsByScope(userId: UUID, scopeId: UUID): List<Long> {
        return UserRoleScopeDao.find(
            UsersRolesScopes.userId eq userId and (UsersRolesScopes.scopeId eq scopeId)
        ).map { it.roleId.value }
    }

    fun hasCapability(userId: UUID, capability: Capability, scopeId: UUID): Boolean = transaction {
        val path = ScopeDao.findById(scopeId)!!.path
        var has = false
        for (nextScopeId in path) {
            when (findCapabilityPermissionOfUserInScope(
                userId,
                nextScopeId,
                capability.toString()
            )) {
                Permission.Undefined -> continue
                Permission.Allow -> has = true
                Permission.Prohibit -> {
                    has = false
                    break
                }
            }
        }
        return@transaction has
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
            findPermissionByRole(
                roleId = usersRolesScopesRow[UsersRolesScopes.roleId].value,
                capabilityResource = capabilityResource
            )
        }.combinedPermission()

    private fun findPermissionByRole(
        roleId: Long,
        capabilityResource: String
    ) = RolesCapabilities.slice(RolesCapabilities.permission)
        .select(
            RolesCapabilities.roleId eq roleId
                    and (RolesCapabilities.capabilityResource eq capabilityResource)
        ).map { rolesCapabilitiesRow -> rolesCapabilitiesRow[RolesCapabilities.permission] }
        .firstOrNull() ?: Permission.Undefined


    fun existRolesByScope(roles: List<Long>, scopeId: UUID): Boolean = transaction {
        roles.all { roleId ->
            val scopeType = ScopeDao.findById(scopeId)!!.scopeTypeId.value
            RolesScopes.exists { RolesScopes.roleId eq roleId and (RolesScopes.scopeId eq scopeType) }
        }
    }

    fun existPermissionRolesByUserId(userId: UUID, assignRoles: List<Long>, scopeId: UUID) = transaction {
        assignRoles.all { assignRole ->
            existPermissionRoleByUserId(userId, assignRole, scopeId)
        }
    }

    private fun existPermissionRoleByUserId(userId: UUID, assignRoleId: Long, scopeId: UUID): Boolean {
        return ScopeDao.findById(scopeId)!!.path.any { segmentScopeId ->
            getUserRoleIdsByScope(userId, segmentScopeId).any { role ->
                existRoleAssignment(role, assignRoleId)
            }
        }
    }


    private fun existRoleAssignment(roleId: Long, assignRoleId: Long): Boolean {
        return RolesAssignments.exists {
            RolesAssignments.roleId eq roleId and (RolesAssignments.assignableRoleId eq assignRoleId)
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

    fun findUserRolesByScopeId(userId: UUID, scopeId: UUID): UserRolesResponse {
        return UserRoleScopeDao.find(
            UsersRolesScopes.userId eq userId
                    and (UsersRolesScopes.scopeId eq scopeId)
        ).toUserRolesResponse(userId)
    }

    fun findUsersByScopeId(scopeId: UUID): List<UserWithRolesResponse> = transaction {
        UserRoleScopeDao.find(UsersRolesScopes.scopeId eq scopeId).toUsersWithRoles()
    }

    fun addUserRolesToScope(userId: UUID, roles: List<Long>, scopeId: UUID) = transaction {
        roles.filterNot { roleId ->
            UsersRolesScopes.exists {
                UsersRolesScopes.userId eq userId and
                        (UsersRolesScopes.roleId eq roleId) and
                        (UsersRolesScopes.scopeId eq scopeId)
            }
        }.map { roleId ->
            UsersRolesScopes.insert {
                it[UsersRolesScopes.userId] = userId
                it[this.scopeId] = scopeId
                it[UsersRolesScopes.roleId] = roleId
            }.run { insertedCount > 0 }
        }.all { it }
    }

    fun removeUserRolesFromScope(userId: UUID, scopeId: UUID) = transaction {
        UsersRolesScopes.deleteWhere {
            UsersRolesScopes.scopeId eq scopeId and
                    (UsersRolesScopes.userId eq userId)
        }
    }.let { it > 0 }

    fun updateByUserAndScope(
        userId: UUID,
        scopeId: UUID,
        updateUserRolesRequest: UpdateUserRolesRequest
    ): UserRolesResponse {
        updateUserRolesRequest.roleIds.let { roles ->
            removeUserRolesFromScope(userId, scopeId)
            addUserRolesToScope(userId, roles, scopeId)
        }
        return findUserRolesByScopeId(userId, scopeId)
    }
}