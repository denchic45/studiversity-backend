package com.studiversity.feature.teacher

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Route.timetableRoute() {
    route("/timetable") {
        get {
            val id = call.request.queryParameters["id"]
            val monday = call.request.queryParameters["monday"]

            //TODO get events by range date and teacher
        }
    }
}