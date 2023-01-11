package com.studiversity.database.table

import com.studiversity.feature.course.element.CourseWorkType
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.time
import java.util.*

object CourseWorks : UUIDTable("course_work", "course_element_id") {
    val dueDate = date("due_date").nullable()
    val dueTime = time("due_time").nullable()
    val type = enumerationByName("work_type", 10, CourseWorkType::class)
}

class CourseWorkDao(id: EntityID<UUID>) : UUIDEntity(id), CourseElementDetailsDao {
    companion object : UUIDEntityClass<CourseWorkDao>(CourseWorks)

    var dueDate by CourseWorks.dueDate
    var dueTime by CourseWorks.dueTime
    var type by CourseWorks.type
}