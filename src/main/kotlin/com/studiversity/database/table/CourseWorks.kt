package com.studiversity.database.table

import com.studiversity.feature.courseelement.WorkType
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
    val workType = enumeration("work_type", WorkType::class)
}

class CourseWorkDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CourseWorkDao>(CourseWorks)

    var dueDate by CourseWorks.dueDate
    var dueTime by CourseWorks.dueTime
    var workType by CourseWorks.workType
}