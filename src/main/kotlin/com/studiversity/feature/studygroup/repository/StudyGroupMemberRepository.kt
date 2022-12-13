package com.studiversity.feature.studygroup.repository

import com.studiversity.database.exists
import com.studiversity.database.table.UserRoleScopeDao
import com.studiversity.database.table.UsersRolesScopes
import com.studiversity.feature.role.Role
import com.studiversity.feature.studygroup.domain.StudyGroupMembers
import com.studiversity.feature.studygroup.mapper.toStudyGroupMembers
import com.studiversity.feature.studygroup.model.UpdateStudyGroupMemberRequest
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
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
        UserRoleScopeDao.find(UsersRolesScopes.scopeId eq groupId).toStudyGroupMembers(groupId)
    }

    fun remove(groupId: UUID, memberId: UUID) = transaction {
        UsersRolesScopes.deleteWhere { scopeId eq groupId and (userId eq memberId) }
    }.let { it > 0 }

    fun isExist(groupId: UUID, memberId: UUID) = transaction {
        UsersRolesScopes.exists { UsersRolesScopes.scopeId eq groupId and (UsersRolesScopes.userId eq memberId) }
    }

    fun update(
        groupId: UUID,
        memberId: UUID,
        updateStudyGroupMemberRequest: UpdateStudyGroupMemberRequest
    ) = transaction {
        updateStudyGroupMemberRequest.roles.ifPresent { roles ->
            remove(groupId, memberId)
            add(groupId, memberId, roles)
        }
    }
}