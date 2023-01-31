package com.studiversity.feature.timetable

import com.studiversity.feature.timetable.usecase.PutTimetableUseCase
import com.studiversity.ktor.getUuid
import com.stuiversity.api.timetable.model.PutTimetableRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.koin.ktor.ext.inject

fun Application.timetableRoutes() {
    routing {
        authenticate("auth-jwt") {
            // TODO validate requests
            route("/studygroups/{studyGroupId}/timetables/{weekOfYear}") {
                val putTimetable: PutTimetableUseCase by inject()
                put {
                    // TODO require capabilities
                    val studyGroupId = call.parameters.getUuid("studyGroupId")
                    val weekOfYear = call.parameters.getOrFail("weekOfYear")
                    val timetable = putTimetable(studyGroupId, weekOfYear, call.receive())
                    call.respond(HttpStatusCode.OK, timetable)
                }
            }
        }
    }
}