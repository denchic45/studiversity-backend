package com.studiversity.feature.course.repository

import com.studiversity.Constants
import com.studiversity.database.table.CourseDao
import com.studiversity.database.table.SubjectDao
import com.studiversity.feature.course.model.CourseResponse
import com.studiversity.feature.course.model.CreateCourseRequest
import com.studiversity.feature.course.toResponse
import com.studiversity.feature.role.ScopeType
import com.studiversity.feature.role.repository.AddScopeRepoExt
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class CourseRepository : AddScopeRepoExt {

    fun add(request: CreateCourseRequest) = transaction {
        val dao = CourseDao.new {
            name = request.name
            request.subjectId?.apply {
                subject = SubjectDao.findById(this)
            }
        }
        addScope(dao.id.value, ScopeType.Course, Constants.organizationId)
        dao.id.value
    }

    fun findById(id: UUID): CourseResponse? = transaction {
        CourseDao.findById(id)?.toResponse()
    }
}