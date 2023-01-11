package com.studiversity.feature.course.element

import com.studiversity.database.table.CourseElementDao
import com.studiversity.database.table.CourseElementDetailsDao
import com.studiversity.database.table.CourseWorkDao
import com.studiversity.feature.course.element.model.CourseElementDetails
import com.studiversity.feature.course.element.model.CourseElementResponse
import com.studiversity.feature.course.element.model.CourseWork

fun CourseElementDao.toResponse(detailsDao: CourseElementDetailsDao): CourseElementResponse = toResponse(
    when (detailsDao) {
        is CourseWorkDao -> detailsDao.toDetailsResponse()
    }
)


private fun CourseElementDao.toResponse(details: CourseElementDetails): CourseElementResponse = CourseElementResponse(
    id = id.value,
    courseId = courseId,
    name = name,
    description = description,
    topicId = topicId,
    order = order,
    details = details
)

private fun CourseWorkDao.toDetailsResponse(): CourseElementDetails = CourseWork(
    dueDate = dueDate,
    dueTime = dueTime,
    workType = type,
    workDetails = null
)
