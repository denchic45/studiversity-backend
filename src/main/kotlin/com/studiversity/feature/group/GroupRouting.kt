package com.studiversity.feature.group

import com.studiversity.feature.group.dto.CreateStudyGroupRequest
import com.studiversity.feature.group.members.groupMembersRoutes
import com.studiversity.feature.group.usecase.AddStudyGroupUseCase
import com.studiversity.feature.group.usecase.FindStudyGroupByIdUseCase
import com.studiversity.util.onlyDigits
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*


fun Application.groupRoutes() {
    routing {
        route("/groups") {
            val addStudyGroup: AddStudyGroupUseCase by inject()
            val findStudyGroupById: FindStudyGroupByIdUseCase by inject()

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
                addStudyGroup(body)
                call.respond(HttpStatusCode.OK, "Group created")
            }
            route("/{id}") {
                get {
                    val id = call.parameters["id"]!!
                    findStudyGroupById(UUID.fromString(id)).let { group ->
                        call.respond(HttpStatusCode.OK, group)
                    }
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