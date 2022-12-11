package com.studiversity.feature.studygroup.repository

import com.studiversity.database.table.RoleDao
import com.studiversity.database.table.UsersRolesScopes
import com.studiversity.feature.studygroup.model.EnrolStudyGroupMemberRequest
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class StudyGroupMemberRepository {

    fun add(groupId: UUID, enrolStudyGroupMemberRequest: EnrolStudyGroupMemberRequest) = transaction {
        enrolStudyGroupMemberRequest.roles.forEach { role ->
            UsersRolesScopes.insert {
                it[userId] = UUID.fromString(enrolStudyGroupMemberRequest.userId)
                it[roleId] = RoleDao.findIdByName(role)
                it[scopeId] = groupId
            }
        }
    }
}