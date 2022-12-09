package com.studiversity.feature.group

import com.studiversity.Constants
import com.studiversity.database.table.SpecialtyDao
import com.studiversity.database.table.StudyGroupDao
import com.studiversity.feature.group.dto.CreateStudyGroupRequest
import com.studiversity.feature.group.dto.StudyGroupResponse
import com.studiversity.feature.group.mapper.toResponse
import com.studiversity.feature.group.mapper.toStudyGroup
import com.studiversity.feature.scope.AddScopeRepoExt
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class StudyGroupRepository : AddScopeRepoExt {

    fun add(request: CreateStudyGroupRequest) = transaction {
        val dao = StudyGroupDao.new {
            name = request.name
            academicYear = arrayOf(request.academicYear.start, request.academicYear.end)
            request.specialtyId?.apply {
                specialty = SpecialtyDao.findById(UUID.fromString(this))
            }
        }
        addScope(dao.id.value, 3, Constants.organizationId)
    }

    fun findById(id: UUID): StudyGroupResponse? {
        return StudyGroupDao.findById(id)?.toResponse()
    }
}