package com.studiversity.feature.role.mapper

import com.studiversity.database.table.RoleDao
import com.studiversity.database.table.UserRoleScopeDao
import com.studiversity.feature.role.Role
import com.studiversity.feature.role.model.UserRolesResponse
import java.util.*

fun RoleDao.toRole(): Role = Role(id = id.value, resource = shortName)

fun Iterable<UserRoleScopeDao>.toUserRolesResponse(userId: UUID): UserRolesResponse = UserRolesResponse(
    userId = userId,
    roles = map { it.role.toRole() }
)