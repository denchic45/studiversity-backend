package com.studiversity.feature.timetable

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.timetableRoutes() {
    routing {
        route("/timetable") {
            get {
                val id = call.request.queryParameters["id"]
                val weekYear: String? = call.request.queryParameters["week_year"]
                val day: String? = call.request.queryParameters["day"]

                //TODO get events by range date and teacher
            }
        }
    }
}