package com.studiversity.feature.course.topic

import com.studiversity.api.course.topic.model.TopicResponse
import com.studiversity.database.table.CourseTopicDao

fun CourseTopicDao.toResponse() = TopicResponse(
    id = id.value,
    name = name
)