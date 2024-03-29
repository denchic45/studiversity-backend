package com.studiversity.feature.studygroup

import com.stuiversity.api.studygroup.model.CreateStudyGroupRequest
import com.stuiversity.api.studygroup.model.UpdateStudyGroupRequest
import com.studiversity.feature.studygroup.usecase.AddStudyGroupUseCase
import com.studiversity.feature.studygroup.usecase.FindStudyGroupByIdUseCase
import com.studiversity.feature.studygroup.usecase.RemoveStudyGroupUseCase
import com.studiversity.feature.studygroup.usecase.UpdateStudyGroupUseCase
import com.studiversity.util.onlyDigits
import com.studiversity.util.toUUID
import com.studiversity.validation.buildValidationResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject


fun Application.studyGroupRoutes() {
    routing {
        authenticate("auth-jwt") {
            route("/studygroups") {

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
                    val response = addStudyGroup(body)
                    call.respond(HttpStatusCode.Created, response)
                }
                studyGroupByIdRoutes()
            }
        }
    }
}

private fun Route.studyGroupByIdRoutes() {
    route("/{id}") {
        val findStudyGroupById: FindStudyGroupByIdUseCase by inject()
        val updateStudyGroup: UpdateStudyGroupUseCase by inject()
        val removeStudyGroup: RemoveStudyGroupUseCase by inject()

        get {
            val id = call.parameters["id"]!!.toUUID()
            findStudyGroupById(id).let { group ->
                call.respond(HttpStatusCode.OK, group)
            }
        }
        patch {
            val id = call.parameters["id"]!!.toUUID()
            val body = call.receive<UpdateStudyGroupRequest>()
            updateStudyGroup(id, body)
            call.respond(HttpStatusCode.OK, "Group updated")
        }
        delete {
            val id = call.parameters["id"]!!.toUUID()
            removeStudyGroup(id)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}