package com.studiversity.feature.course.element.repository

import com.studiversity.database.exists
import com.studiversity.database.table.CourseElementDao
import com.studiversity.database.table.CourseElements
import com.studiversity.database.table.CourseWorkDao
import com.studiversity.feature.course.element.CourseElementType
import com.studiversity.feature.course.element.model.CourseElementResponse
import com.studiversity.feature.course.element.model.UpdateCourseElementRequest
import com.studiversity.feature.course.element.toResponse
import com.studiversity.feature.course.work.model.CreateCourseWorkRequest
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.minus
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.util.*

class CourseElementRepository {

    fun addWork(courseId: UUID, request: CreateCourseWorkRequest): CourseElementResponse {
        val elementId = UUID.randomUUID()
        return CourseElementDao.new(elementId) {
            this.courseId = courseId
            this.topicId = request.topicId
            this.name = request.name
            this.type = CourseElementType.WORK
            this.order = generateOrderByCourseAndTopicId(courseId, request.topicId)
        }.toResponse(
            CourseWorkDao.new(elementId) {
                this.dueDate = request.dueDate
                this.dueTime = request.dueTime
                this.type = request.workType
                this.maxGrade = request.maxGrade
            }
        )
    }

    private fun generateOrderByCourseAndTopicId(courseId: UUID, topicId: UUID?) =
        CourseElementDao.getMaxOrderByCourseIdAndTopicId(courseId, topicId) + 1


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
            .singleOrNull()?.apply {
                CourseElements.update(where = { CourseElements.order greater this@apply.order }) {
                    it[order] = order - 1
                }
            }
            ?.delete() != null
    }

    fun findMaxGradeByWorkId(workId: UUID): Short {
        return CourseWorkDao.findById(workId)!!.maxGrade
    }

    fun findTypeByElementId(elementId: UUID): CourseElementType? {
        return CourseElements.select(CourseElements.id eq elementId)
            .singleOrNull()?.let { it[CourseElements.type] }
    }

    fun exist(courseId: UUID, elementId: UUID) = CourseElements.exists {
        CourseElements.id eq elementId and (CourseElements.courseId eq courseId)
    }

    fun update(
        courseId: UUID,
        elementId: UUID,
        updateCourseElementRequest: UpdateCourseElementRequest
    ): CourseElementResponse {
        val dao = CourseElementDao.findById(elementId)!!

        CourseElements.update(where = { CourseElements.order greater dao.order }) {
            it[order] = order - 1
        }

        updateCourseElementRequest.topicId.ifPresent { topicId ->
            dao.order = generateOrderByCourseAndTopicId(courseId, topicId)
            dao.topicId = topicId
        }

        return dao.toResponse(getElementDetails(dao.type, elementId))
    }

    private fun getElementDetails(
        type: CourseElementType,
        elementId: UUID
    ) = when (type) {
        CourseElementType.WORK -> CourseWorkDao.findById(elementId)!!
        CourseElementType.MATERIAL -> TODO()
    }
}