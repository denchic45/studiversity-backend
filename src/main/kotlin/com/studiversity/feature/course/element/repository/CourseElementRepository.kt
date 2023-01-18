package com.studiversity.feature.course.element.repository

import com.studiversity.database.table.CourseElementDao
import com.studiversity.database.table.CourseElements
import com.studiversity.database.table.CourseWorkDao
import com.studiversity.feature.course.element.CourseElementType
import com.studiversity.feature.course.element.model.CourseElementResponse
import com.studiversity.feature.course.element.model.CreateCourseWorkRequest
import com.studiversity.feature.course.element.toResponse
import com.studiversity.supabase.deleteRecursive
import io.github.jan.supabase.storage.BucketApi
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class CourseElementRepository(private val bucket: BucketApi) {

    fun addWork(courseId: UUID, request: CreateCourseWorkRequest): CourseElementResponse {
        val elementId = UUID.randomUUID()

        return CourseElementDao.new(elementId) {
            this.courseId = courseId
            this.topicId = request.topicId
            this.name = request.name
            this.type = CourseElementType.Work
            this.order = CourseElements.slice(CourseElements.order.max())
                .selectAll()
                .single()[CourseElements.order.max()]?.let { it + 1 } ?: 1
        }.toResponse(
            CourseWorkDao.new(elementId) {
                this.dueDate = request.dueDate
                this.dueTime = request.dueTime
                this.type = request.workType
                this.maxGrade = request.maxGrade
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

    suspend fun remove(courseId: UUID, elementId: UUID): Boolean {
        bucket.deleteRecursive("courses/$courseId/elements/$elementId")
        return CourseElementDao.findById(elementId)?.delete() != null
    }

    fun findMaxGradeByWorkId(workId: UUID): Short {
        return CourseWorkDao.findById(workId)!!.maxGrade
    }
}