package com.studiversity.feature.course.element.repository

import com.studiversity.database.table.CourseElementDao
import com.studiversity.database.table.CourseElements
import com.studiversity.database.table.CourseWorkDao
import com.studiversity.feature.course.element.CourseElementType
import com.studiversity.feature.course.element.model.CourseElementResponse
import com.studiversity.feature.course.element.model.CreateCourseWorkRequest
import com.studiversity.feature.course.element.toResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.select
import java.util.*

class CourseElementRepository {

    fun addWork(courseId: UUID, request: CreateCourseWorkRequest): CourseElementResponse {
        val elementId = UUID.randomUUID()
        return CourseElementDao.new(elementId) {
            this.courseId = courseId
            this.topicId = request.topicId
            this.name = request.name
            this.type = CourseElementType.WORK
            this.order = generateOrderByCourseAndTopicId(courseId)
        }.toResponse(
            CourseWorkDao.new(elementId) {
                this.dueDate = request.dueDate
                this.dueTime = request.dueTime
                this.type = request.workType
                this.maxGrade = request.maxGrade
            }
        )
    }

    private fun generateOrderByCourseAndTopicId(courseId: UUID) = findMaxOrderByCourseIdAndTopicId(courseId) + 1

    private fun findMaxOrderByCourseIdAndTopicId(courseId: UUID, topicId: UUID? = null): Int {
        val query = CourseElements.slice(CourseElements.order.max()).select(CourseElements.courseId eq courseId)
        if (topicId != null) query.andWhere { CourseElements.topicId eq topicId }
        return query.single().let { it[CourseElements.order.max()] ?: 0 }
    }

    fun findById(elementId: UUID): CourseElementResponse? {
        return CourseElementDao.findById(elementId)?.run {
            toResponse(
                when (type) {
                    CourseElementType.WORK -> CourseWorkDao.findById(elementId)!!
                    CourseElementType.MATERIAL -> TODO()
                }
            )
        }
    }

    fun findCourseIdByElementId(elementId: UUID): UUID? {
        return CourseElementDao.findById(elementId)?.courseId
    }

    fun remove(courseId: UUID, elementId: UUID): Boolean {
        return CourseElementDao.find(CourseElements.courseId eq courseId and (CourseElements.id eq elementId))
            .singleOrNull()
            ?.delete() != null
    }

    fun findMaxGradeByWorkId(workId: UUID): Short {
        return CourseWorkDao.findById(workId)!!.maxGrade
    }

    fun findTypeByElementId(elementId: UUID): CourseElementType? {
        return CourseElements.select(CourseElements.id eq elementId)
            .singleOrNull()?.let { it[CourseElements.type] }
    }
}