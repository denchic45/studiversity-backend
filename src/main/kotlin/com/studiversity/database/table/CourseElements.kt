package com.studiversity.database.table

import com.studiversity.database.type.timestampWithTimeZone
import com.studiversity.feature.courseelement.ElementType
import com.studiversity.util.varcharMax
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import java.util.*

object CourseElements : UUIDTable("course_element", "course_element_id") {
    val courseId = uuid("course_id").references(Courses.id)
    val topicId = uuid("topic_id").references(CourseTopics.id).nullable()
    val name = varcharMax("element_name")
    val description = text("description").nullable()
    val order = integer("order")
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestamp())
    val updatedAt = timestampWithTimeZone("updated_at").nullable()
    val type = enumeration("element_type", ElementType::class)
}

class CourseElementDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CourseElementDao>(CourseElements)

    var courseId by CourseElements.courseId
    var topicId by CourseElements.topicId
    var name by CourseElements.name
    var description by CourseElements.description
    var order by CourseElements.order
    var createdAt by CourseElements.createdAt
    var updatedAt by CourseElements.updatedAt
    var type by CourseElements.type

    val work by CourseWorkDao referrersOn CourseWorks.id
    // todo add posts relation
}