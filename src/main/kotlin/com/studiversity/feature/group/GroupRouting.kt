package com.studiversity.feature.group

import com.studiversity.feature.group.members.groupMembersRoutes
import com.studiversity.util.onlyDigits
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*


fun Application.groupRoutes() {
    routing {
        route("/groups") {

            val studyGroupRepository: StudyGroupRepository by inject()

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
            post {
                val body = call.receive<CreateStudyGroupRequest>()
                studyGroupRepository.add(body)
                call.respond(HttpStatusCode.OK, "Group created")
            }
            route("/{id}") {
                get {
                    val id = call.parameters["id"]!!
                    studyGroupRepository.findById(UUID.fromString(id))?.let { group ->
                        call.respond(HttpStatusCode.OK, group)
                    } ?: throw NotFoundException()
                }
                patch {

                }
                post("/archive") {

                }
                delete { }
            }

            groupMembersRoutes()
        }
    }
}