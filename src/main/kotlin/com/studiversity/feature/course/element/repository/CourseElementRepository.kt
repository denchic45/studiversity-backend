package com.studiversity.feature.course.element.repository

import com.studiversity.database.table.CourseElementDao
import com.studiversity.database.table.CourseElements
import com.studiversity.database.table.CourseWorkDao
import com.studiversity.feature.course.element.CourseElementType
import com.studiversity.feature.course.element.model.CourseElementResponse
import com.studiversity.feature.course.element.model.CreateCourseElementRequest
import com.studiversity.feature.course.element.model.CreateCourseWorkRequest
import com.studiversity.feature.course.element.toResponse
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class CourseElementRepository {

    fun add(courseId: UUID, request: CreateCourseElementRequest): CourseElementResponse {
        val elementId = UUID.randomUUID()
        val type = request.type

        return CourseElementDao.new(elementId) {
            this.courseId = courseId
            this.topicId = request.topicId
            this.name = request.name
            this.type = type
            this.order = CourseElements.slice(CourseElements.order.max())
                .selectAll()
                .single()[CourseElements.order.max()]?.let { it + 1 } ?: 1
        }.toResponse(
            when (request) {
                is CreateCourseWorkRequest -> {
                    CourseWorkDao.new(elementId) {
                        this.dueDate = request.details.dueDate
                        this.dueTime = request.details.dueTime
                        this.type = request.details.workType
                        this.maxGrade = request.details.maxGrade
                    }
                }
            }
        )
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

    fun findMaxGradeByWorkId(workId: UUID): Short {
      return  CourseWorkDao.findById(workId)!!.maxGrade
    }
}