package com.studiversity.feature.courseelement

import com.studiversity.database.table.CourseElementDao
import com.studiversity.database.table.CourseWorkDao
import com.studiversity.feature.courseelement.model.CourseElementDetails
import com.studiversity.feature.courseelement.model.CourseElementResponse

fun CourseElementDao.toResponse(): CourseElementResponse = CourseElementResponse(
    courseId = courseId,
    topicId = topicId,
    order = order,
    details = when (type) {
        ElementType.Work -> work.single().toResponse()
        ElementType.Post -> TODO("Add post mapping")
    }
)

fun CourseWorkDao.toResponse(): CourseElementDetails.Work = CourseElementDetails.Work(
    dueDate = dueDate,
    dueTime = dueTime,
    workType = workType
)