package com.studiversity.feature.teacher

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Route.timetableRoute() {
    route("/timetable") {
        get {
            val weekYear = call.request.queryParameters["week_year"]
            //TODO get events by range date and teacher
        }
    }
}