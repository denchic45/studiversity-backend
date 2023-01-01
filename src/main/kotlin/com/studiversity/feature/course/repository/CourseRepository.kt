package com.studiversity.feature.course.repository

import com.studiversity.database.exists
import com.studiversity.database.table.CourseDao
import com.studiversity.database.table.Courses
import com.studiversity.database.table.SubjectDao
import com.studiversity.feature.course.model.CourseResponse
import com.studiversity.feature.course.model.CreateCourseRequest
import com.studiversity.feature.course.model.UpdateCourseRequest
import com.studiversity.feature.course.toResponse
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class CourseRepository {

    fun add(request: CreateCourseRequest): UUID {
        val dao = CourseDao.new {
            name = request.name
            request.subjectId?.apply {
                subject = SubjectDao.findById(this)
            }
        }
        return dao.id.value
    }

    fun findById(id: UUID): CourseResponse? {
        return CourseDao.findById(id)?.toResponse()
    }

    fun update(id: UUID, request: UpdateCourseRequest) = transaction {
        CourseDao.findById(id)?.apply {
            request.name.ifPresent { name = it }
            request.subjectId.ifPresent { subject = it?.let { SubjectDao.findById(it) } }
        }?.toResponse()
    }

    fun exist(id: UUID): Boolean {
       return Courses.exists { Courses.id eq id }
    }
}