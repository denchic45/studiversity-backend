package com.studiversity.database.table

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object Lessons : LongIdTable("lesson", "period_id") {
    val courseId = CourseElements.uuid("course_id").references(
        Courses.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )

    init {
        foreignKey(
            from = arrayOf(id),
            target = Periods.primaryKey,
            onUpdate = ReferenceOption.CASCADE,
            onDelete = ReferenceOption.CASCADE,
            "lesson_course_id_fkey"
        )
    }
}