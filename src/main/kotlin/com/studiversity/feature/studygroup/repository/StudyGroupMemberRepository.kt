package com.studiversity.feature.studygroup.repository

import com.studiversity.database.table.UserRoleScopeDao
import com.studiversity.database.table.UsersRolesScopes
import com.studiversity.feature.role.Role
import com.studiversity.feature.studygroup.domain.StudyGroupMembers
import com.studiversity.feature.studygroup.mapper.toStudyGroupMember
import com.studiversity.feature.studygroup.mapper.toStudyGroupMembers
import com.studiversity.feature.role.model.UpdateUserRolesRequest
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class StudyGroupMemberRepository {

    fun findByGroupId(groupId: UUID): StudyGroupMembers = transaction {
        UserRoleScopeDao.find(UsersRolesScopes.scopeId eq groupId).toStudyGroupMembers(groupId)
    }
}