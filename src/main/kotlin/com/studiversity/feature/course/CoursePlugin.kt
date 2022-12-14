package com.studiversity.feature.course

import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

@Suppress("unused")
fun Application.coursesModule() {
    courseRoutes()
}

object CourseErrors