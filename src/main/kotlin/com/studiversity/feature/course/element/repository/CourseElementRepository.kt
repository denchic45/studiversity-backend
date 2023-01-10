package com.studiversity.feature.course.element.repository

import com.studiversity.database.table.CourseElementDao
import com.studiversity.database.table.CourseElements
import com.studiversity.database.table.CourseWorkDao
import com.studiversity.feature.course.element.ElementType
import com.studiversity.feature.course.element.model.CourseElementDetails
import com.studiversity.feature.course.element.model.CourseElementResponse
import com.studiversity.feature.course.element.model.CreateCourseElementRequest
import com.studiversity.feature.course.element.toResponse
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class CourseElementRepository {

    fun add(courseId: UUID, request: CreateCourseElementRequest): CourseElementResponse {
        val elementId = UUID.randomUUID()
        val type = when (request.details) {
            is CourseElementDetails.Work -> ElementType.Work
            is CourseElementDetails.Post -> ElementType.Post
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
            is CourseElementDetails.Work -> CourseWorkDao.new(elementId) {
                this.dueDate = details.dueDate
                this.dueTime = details.dueTime
                this.type = details.workType
            }

            is CourseElementDetails.Post -> TODO()
        })
    }

    fun findById(elementId: UUID): CourseElementResponse? {
        return CourseElementDao.findById(elementId)?.run {
            toResponse(
                when (type) {
                    ElementType.Work -> CourseWorkDao.findById(elementId)!!
                    ElementType.Post -> TODO()
                }
            )
        }
    }

    fun remove(elementId: UUID):Boolean {
       return CourseElementDao.findById(elementId)?.delete() != null
    }
}