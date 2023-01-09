package com.studiversity.feature.course

import com.studiversity.feature.course.element.courseElementRoutes
import com.studiversity.feature.course.subject.subjectRoutes
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

@Suppress("unused")
fun Application.coursesModule() {
    courseRoutes()
    courseElementRoutes()
    subjectRoutes()
}

object CourseErrors {
    const val INVALID_COURSE_NAME = "INVALID_COURSE_NAME"
    const val STUDY_GROUP_ALREADY_EXIST = "STUDY_GROUP_ALREADY_EXIST"
    const val COURSE_IS_NOT_ARCHIVED = "COURSE_IS_NOT_ARCHIVED"
}