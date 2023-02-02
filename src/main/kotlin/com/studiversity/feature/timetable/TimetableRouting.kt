package com.studiversity.feature.timetable

import com.studiversity.di.OrganizationEnv
import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import com.studiversity.feature.timetable.usecase.FindTimetableByStudyGroupUseCase
import com.studiversity.feature.timetable.usecase.PutTimetableUseCase
import com.studiversity.ktor.currentUserId
import com.studiversity.ktor.getSortingBy
import com.studiversity.util.toUUID
import com.stuiversity.api.timetable.model.SortingPeriods
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject
import java.util.*

fun Application.timetableRoutes() {
    routing {
        authenticate("auth-jwt") {
            // TODO validate requests
            route("/timetables/{weekOfYear}") {
                val putTimetable: PutTimetableUseCase by inject()
                val findTimetableByStudyGroup: FindTimetableByStudyGroupUseCase by inject()
                val requireCapability: RequireCapabilityUseCase by inject()
                val organizationId: UUID by inject(named(OrganizationEnv.ORG_ID))
                put {
                    requireCapability(call.currentUserId(), Capability.WriteTimetable, organizationId)

                    val weekOfYear = call.parameters.getOrFail("weekOfYear")
                    val timetable = putTimetable(weekOfYear, call.receive())
                    call.respond(HttpStatusCode.OK, timetable)
                }
                get {
                    val weekOfYear = call.parameters.getOrFail("weekOfYear")

                    val studyGroupIds = call.request.queryParameters.getAll("studyGroupId")?.map(String::toUUID)
                    val courseIds = call.request.queryParameters.getAll("courseId")?.map(String::toUUID)
                    val memberIds = call.request.queryParameters.getAll("memberId")?.map(String::toUUID)
                    val roomIds = call.request.queryParameters.getAll("roomId")?.map(String::toUUID)

                    if (studyGroupIds == null && courseIds == null && memberIds == null && roomIds == null)
                        throw MissingRequestParameterException("period field")

                    val timetable = findTimetableByStudyGroup(
                        studyGroupIds,
                        courseIds,
                        memberIds,
                        roomIds,
                        weekOfYear,
                        call.request.queryParameters.getSortingBy(SortingPeriods)
                    )
                    call.respond(HttpStatusCode.OK, timetable)
                }
            }
        }
    }
}