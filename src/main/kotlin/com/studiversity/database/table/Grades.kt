package com.studiversity.database.table

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import java.util.*

object Grades : LongIdTable("grade", "grade_id") {
    val courseId = uuid("course_id").references(Courses.id)
    val studentId = uuid("student_id").references(Users.id)
    val gradedBy = uuid("graded_by").references(Users.id).nullable()
    val value = short("value")
    val submissionId: Column<EntityID<UUID>?> = reference("submission_id", Submissions).nullable()
}

class GradeDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<GradeDao>(Grades)

    var courseId by Grades.courseId
    var studentId by Grades.studentId
    var gradedBy by Grades.gradedBy
    var value by Grades.value
    var submissionId by Grades.submissionId

    var submission by SubmissionDao optionalReferencedOn Grades.submissionId
}