package com.studiversity.feature.courseelement.repository

import com.studiversity.database.table.CourseElementDao
import com.studiversity.database.table.CourseWorkDao
import com.studiversity.feature.courseelement.ElementType
import com.studiversity.feature.courseelement.model.CourseElementDetails
import com.studiversity.feature.courseelement.model.CourseElementResponse
import com.studiversity.feature.courseelement.model.CreateCourseElementRequest
import com.studiversity.feature.courseelement.toResponse
import java.util.*

class CourseElementRepository {

    fun add(courseId: UUID, request: CreateCourseElementRequest): CourseElementResponse {
        val elementId = UUID.randomUUID()
        return CourseElementDao.new(elementId) {
            this.courseId = courseId
            topicId = request.topicId
            type = when (val details = request.details) {
                is CourseElementDetails.Work -> {
                    CourseWorkDao.new(elementId) {
                        dueDate = details.dueDate
                        dueTime = details.dueTime
                        workType = details.workType
                    }
                    ElementType.Work
                }

                is CourseElementDetails.Post -> {
                    // todo create post
                    ElementType.Post
                }
            }
        }.toResponse()
    }
}