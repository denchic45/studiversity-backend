package com.studiversity.feature.course.element

import com.studiversity.database.table.CourseElementDao
import com.studiversity.database.table.CourseElementDetailsDao
import com.studiversity.database.table.CourseWorkDao
import com.studiversity.feature.course.element.model.CourseElementDetails
import com.studiversity.feature.course.element.model.CourseElementResponse

fun CourseElementDao.toResponse(detailsDao: CourseElementDetailsDao): CourseElementResponse = toResponse(
    when (detailsDao) {
        is CourseWorkDao -> CourseElementDetails.Work(
            dueDate = detailsDao.dueDate,
            dueTime = detailsDao.dueTime,
            workType = detailsDao.type
        )
    }
)


private fun CourseElementDao.toResponse(details: CourseElementDetails): CourseElementResponse = CourseElementResponse(
    courseId = courseId,
    name = name,
    description = description,
    topicId = topicId,
    order = order,
    details = details
)

fun CourseWorkDao.toResponse(): CourseElementDetails.Work = CourseElementDetails.Work(
    dueDate = dueDate,
    dueTime = dueTime,
    workType = type
)