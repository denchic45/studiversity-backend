package com.studiversity.feature.course

import com.studiversity.feature.course.subject.subjectRoutes
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

@Suppress("unused")
fun Application.coursesModule() {
    courseRoutes()
    subjectRoutes()
}

object CourseErrors