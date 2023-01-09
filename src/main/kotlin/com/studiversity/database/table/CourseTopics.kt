package com.studiversity.database.table

import com.studiversity.util.varcharMax
import org.jetbrains.exposed.dao.id.UUIDTable

object CourseTopics : UUIDTable("course_topic", "course_topic_id") {
    val courseId = reference("course_id", Courses.id)
    val name = varcharMax("topic_name")
}