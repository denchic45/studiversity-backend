package com.studiversity.feature.studygroup

import com.studiversity.feature.studygroup.model.CreateStudyGroupRequest
import com.studiversity.feature.studygroup.model.UpdateStudyGroupRequest
import com.studiversity.feature.studygroup.usecase.AddStudyGroupUseCase
import com.studiversity.feature.studygroup.usecase.FindStudyGroupByIdUseCase
import com.studiversity.feature.studygroup.usecase.RemoveStudyGroupUseCase
import com.studiversity.feature.studygroup.usecase.UpdateStudyGroupUseCase
import com.studiversity.util.onlyDigits
import com.studiversity.validation.buildValidationResult
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


            install(RequestValidation) {
                validate<CreateStudyGroupRequest> { request ->
                    buildList {
                        if (request.name.isEmpty() || request.name.onlyDigits())
                            add(StudyGroupErrors.INVALID_GROUP_NAME)

                        if (request.academicYear.run { start > end })
                            add(StudyGroupErrors.INVALID_ACADEMIC_YEAR)

                    }.let { errors ->
                        if (errors.isEmpty())
                            ValidationResult.Valid
                        else ValidationResult.Invalid(errors)
                    }
                }
                validate<UpdateStudyGroupRequest> { request ->
                    buildValidationResult {
                        request.name.ifPresent {
                            condition(it.isNotEmpty() && !it.onlyDigits(), StudyGroupErrors.INVALID_GROUP_NAME)
                        }
                        request.academicYear.ifPresent {
                            condition(it.run { start <= end }, StudyGroupErrors.INVALID_ACADEMIC_YEAR)
                        }
                    }
                }
            }
            post {
                val body = call.receive<CreateStudyGroupRequest>()
                val id = addStudyGroup(body).toString()
                call.respond(HttpStatusCode.Created, id)
            }
            groupRoute()
        }
    }
}

private fun Route.groupRoute() {
    route("/{id}") {

        val findStudyGroupById: FindStudyGroupByIdUseCase by inject()
        val updateStudyGroup: UpdateStudyGroupUseCase by inject()
        val removeStudyGroup: RemoveStudyGroupUseCase by inject()

        get {
            val id = UUID.fromString(call.parameters["id"]!!)
            findStudyGroupById(id).let { group ->
                call.respond(HttpStatusCode.OK, group)
            }
        }
        patch {
            val id = UUID.fromString(call.parameters["id"]!!)
            val body = call.receive<UpdateStudyGroupRequest>()
            updateStudyGroup(id, body)
            call.respond(HttpStatusCode.OK, "Group updated")
        }
        post("/archive") {

        }
        delete {
            val id = UUID.fromString(call.parameters["id"]!!)
            removeStudyGroup(id)
            call.respond(HttpStatusCode.NoContent, "Group deleted")
        }

        groupMembersRoutes()

    }
}