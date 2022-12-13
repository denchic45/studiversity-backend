package com.studiversity.feature.role.mapper

import com.studiversity.database.table.RoleDao
import com.studiversity.feature.role.Role

fun RoleDao.toRole(): Role = Role(id = id.value, resource = shortName)

fun Iterable<RoleDao>.toRoles(): List<Role> = map(RoleDao::toRole)