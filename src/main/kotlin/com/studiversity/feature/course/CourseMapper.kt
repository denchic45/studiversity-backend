package com.studiversity.feature.course

import com.studiversity.database.table.CourseDao
import com.studiversity.feature.course.model.CourseResponse

fun CourseDao.toResponse(): CourseResponse = CourseResponse(
    id = id.value,
    name = name
)