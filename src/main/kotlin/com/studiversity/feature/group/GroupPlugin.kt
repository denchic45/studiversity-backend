package com.studiversity.feature.group

import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

@Suppress("unused")
fun Application.groupsModule() {
    groupRoutes()
}

object GroupErrors {
    const val INVALID_GROUP_NAME = "INVALID_GROUP_NAME"
    const val INVALID_ACADEMIC_YEAR = "INVALID_ACADEMIC_YEAR"
}