package com.studiversity.feature.studygroup.repository

import com.studiversity.database.table.SpecialtyDao
import com.studiversity.database.table.StudyGroupDao
import com.studiversity.database.table.StudyGroups
import com.studiversity.feature.studygroup.mapper.toResponse
import com.studiversity.feature.studygroup.model.CreateStudyGroupRequest
import com.studiversity.feature.studygroup.model.StudyGroupResponse
import com.studiversity.feature.studygroup.model.UpdateStudyGroupRequest
import com.studiversity.util.toUUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import java.util.*

class StudyGroupRepository {

    fun add(request: CreateStudyGroupRequest): UUID {
        val dao = StudyGroupDao.new {
            name = request.name
            academicYear = listOf(request.academicYear.start, request.academicYear.end)
            request.specialtyId?.apply {
                specialty = SpecialtyDao.findById(this)
            }
        }
        return dao.id.value
    }

    fun update(id: UUID, updateStudyGroupRequest: UpdateStudyGroupRequest): Boolean {
        return StudyGroups.update({ StudyGroups.id eq id }) { update ->
            updateStudyGroupRequest.apply {
                name.ifPresent { update[StudyGroups.name] = it }
                academicYear.ifPresent {
                    update[StudyGroups.academicYear.column] = arrayOf(it.start.toShort(), it.end.toShort())
                }
                specialtyId.ifPresent { update[StudyGroups.specialtyId] = it?.toUUID() }
            }
        }.run { this != 0 }
    }

    fun findById(id: UUID): StudyGroupResponse? = StudyGroupDao.findById(id)?.toResponse()

    fun remove(id: UUID) = StudyGroups.deleteWhere { StudyGroups.id eq id }.run { this != 0 }
}