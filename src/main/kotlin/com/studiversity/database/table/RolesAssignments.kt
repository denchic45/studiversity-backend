package com.studiversity.database.table

import org.jetbrains.exposed.dao.id.LongIdTable

object RolesAssignments : LongIdTable("role_assignment", "role_assignment_id") {
    val roleId = reference("role_id", Roles.id)
    val assignRoleId = reference("assign_role", Roles.id)
}