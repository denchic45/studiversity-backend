package com.studiversity.feature.timetable

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.timetableRoutes() {
    routing {
        authenticate("auth-jwt") {
            route("/studygroups/{studyGroupId}/timetables/{week}") {
                put {

                }
            }
        }
    }
}