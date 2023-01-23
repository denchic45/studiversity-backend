package com.studiversity.database.table

import com.studiversity.util.varcharMax
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

object CourseTopics : UUIDTable("course_topic", "course_topic_id") {
    val courseId = uuid("course_id").references(Courses.id)
    val name = varcharMax("topic_name")
    val order = integer("topic_order")
}

class CourseTopicDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CourseTopicDao>(CourseTopics)

    var courseId by CourseTopics.courseId
    var name by CourseTopics.name
    var order by CourseTopics.order
}