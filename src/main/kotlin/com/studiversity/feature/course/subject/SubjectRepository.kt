package com.studiversity.feature.course.subject

import com.studiversity.database.table.CourseDao
import com.studiversity.database.table.Courses
import com.studiversity.database.table.SubjectDao
import com.stuiversity.api.course.subject.model.CreateSubjectRequest
import com.stuiversity.api.course.subject.model.UpdateSubjectRequest
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class SubjectRepository {

    fun add(request: CreateSubjectRequest) = transaction {
        SubjectDao.new {
            name = request.name
            iconName = request.iconName
        }.id.value
    }

    fun findById(id: UUID) = transaction {
        SubjectDao.findById(id)?.toResponse()
    }

    fun findAll() = transaction {
        SubjectDao.all().toResponses()
    }

    fun update(id: UUID, request: UpdateSubjectRequest) = transaction {
        SubjectDao.findById(id)?.apply {
            request.name.ifPresent { name = it }
            request.iconName.ifPresent { iconName = it }
        }.run { this?.toResponse() }
    }

    fun remove(id: UUID) = transaction {
        val subjectDao = SubjectDao.findById(id) ?: return@transaction null
        CourseDao.find(Courses.subjectId eq id).forEach {
            it.subjectId = null
        }
        subjectDao.delete()
    }
}