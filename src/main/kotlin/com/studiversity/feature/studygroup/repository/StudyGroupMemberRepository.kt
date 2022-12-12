package com.studiversity.feature.studygroup.repository

import com.studiversity.database.table.UserRoleScopeDao
import com.studiversity.database.table.UsersRolesScopes
import com.studiversity.feature.role.Role
import com.studiversity.feature.studygroup.domain.StudyGroupMembers
import com.studiversity.feature.studygroup.mapper.toStudyGroupMembers
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class StudyGroupMemberRepository {

    fun add(groupId: UUID, userId: UUID, roles: List<Role>) = transaction {
        roles.map { role ->
            UsersRolesScopes.insert {
                it[UsersRolesScopes.userId] = userId
                it[roleId] = role.id
                it[scopeId] = groupId
            }.run { insertedCount > 0 }
        }.all { it }
    }

    fun findByGroupId(groupId: UUID): StudyGroupMembers = transaction {
        UserRoleScopeDao.find(UsersRolesScopes.scopeId eq groupId).toStudyGroupMembers()
    }
}