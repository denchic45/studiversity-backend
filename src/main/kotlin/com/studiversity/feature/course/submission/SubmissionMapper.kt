package com.studiversity.feature.course.submission

import com.studiversity.database.table.SubmissionDao
import com.studiversity.feature.course.element.CourseWorkType
import com.studiversity.feature.course.submission.model.AssignmentSubmissionResponse
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun SubmissionDao.toResponse() = when (courseWork.type) {
    CourseWorkType.Assignment -> AssignmentSubmissionResponse(
        id = id.value,
        authorId = authorId,
        state = state,
        courseWorkId = courseWorkId,
        content = content?.let { Json.decodeFromString(it) },
    )
}