package com.studiversity.feature.group

import com.studiversity.feature.group.members.groupMembersRoutes
import com.studiversity.util.onlyDigits
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject


fun Application.groupRoutes() {
    routing {
        route("/groups") {
            install(RequestValidation) {
                validate<CreateStudyGroupRequest> { request ->
                    buildList {
                        if (request.name.isEmpty() || request.name.onlyDigits())
                            add(GroupErrors.INVALID_GROUP_NAME)

                        if (request.academicYear.run { start > end })
                            add(GroupErrors.INVALID_ACADEMIC_YEAR)

                    }.let { errors ->
                        if (errors.isEmpty())
                            ValidationResult.Valid
                        else ValidationResult.Invalid(errors)
                    }
                }
            }
            val studyGroupRepository: StudyGroupRepository by inject()
            post {
                val body = call.receive<CreateStudyGroupRequest>()
                studyGroupRepository.add(body)
                call.respond(HttpStatusCode.OK, "Group created")
            }
            get {

            }
            patch { }
            post("/archive") {

            }
            delete { }

            groupMembersRoutes()
        }
    }
}