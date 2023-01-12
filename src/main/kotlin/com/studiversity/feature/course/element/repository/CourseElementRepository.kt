package com.studiversity.feature.course.element.repository

import com.studiversity.database.table.CourseElementDao
import com.studiversity.database.table.CourseElements
import com.studiversity.database.table.CourseWorkDao
import com.studiversity.feature.course.element.CourseElementType
import com.studiversity.feature.course.element.model.CourseElementResponse
import com.studiversity.feature.course.element.model.CourseMaterial
import com.studiversity.feature.course.element.model.CourseWork
import com.studiversity.feature.course.element.model.CreateCourseElementRequest
import com.studiversity.feature.course.element.toResponse
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class CourseElementRepository {

    fun add(courseId: UUID, request: CreateCourseElementRequest): CourseElementResponse {
        val elementId = UUID.randomUUID()
        val type = when (request.details) {
            is CourseWork -> CourseElementType.Work
            is CourseMaterial -> CourseElementType.Material
        }
        return CourseElementDao.new(elementId) {
            this.courseId = courseId
            this.topicId = request.topicId
            this.name = request.name
            this.type = type
            this.order = CourseElements.slice(CourseElements.order.max())
                .selectAll()
                .single()[CourseElements.order.max()]?.let { it + 1 } ?: 1
        }.toResponse(when (val details = request.details) {
            is CourseWork -> CourseWorkDao.new(elementId) {
                this.dueDate = details.dueDate
                this.dueTime = details.dueTime
                this.type = details.workType
            }

            is CourseMaterial -> TODO()
        })
    }

    fun findById(elementId: UUID): CourseElementResponse? {
        return CourseElementDao.findById(elementId)?.run {
            toResponse(
                when (type) {
                    CourseElementType.Work -> CourseWorkDao.findById(elementId)!!
                    CourseElementType.Material -> TODO()
                }
            )
        }
    }

    fun findCourseIdByElementId(elementId: UUID): UUID? {
        return CourseElementDao.findById(elementId)?.courseId
    }

    fun remove(elementId: UUID): Boolean {
        return CourseElementDao.findById(elementId)?.delete() != null
    }
}