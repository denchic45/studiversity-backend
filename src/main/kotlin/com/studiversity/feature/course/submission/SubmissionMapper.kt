package com.studiversity.feature.course.submission

import com.studiversity.database.table.SubmissionDao
import com.studiversity.feature.course.element.CourseWorkType
import com.studiversity.feature.course.submission.model.Submission
import com.studiversity.feature.course.submission.model.SubmissionContent
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun SubmissionDao.toResponse() = Submission(
    id = id.value,
    authorId = authorId,
    courseWorkId = courseWorkId,
    content = toContent()
)

private fun SubmissionDao.toContent(): SubmissionContent? = content?.let {
    when (courseWork.type) {
        CourseWorkType.Assignment -> Json.decodeFromString(it)
    }
}