package com.studiversity.feature.course.subject

import com.studiversity.di.OrganizationEnv
import com.studiversity.feature.course.subject.usecase.*
import com.stuiversity.api.role.model.Capability
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import com.studiversity.ktor.claimId
import com.studiversity.ktor.jwtPrincipal
import com.studiversity.util.onlyDigits
import com.studiversity.util.toUUID
import com.studiversity.validation.buildValidationResult
import com.stuiversity.api.course.subject.model.CreateSubjectRequest
import com.stuiversity.api.course.subject.model.UpdateSubjectRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.qualifier.named
import org.koin.ktor.ext.inject
import java.util.*

fun Application.subjectRoutes() {
    routing {
        authenticate("auth-jwt") {
            route("/subjects") {
                install(RequestValidation) {
                    validate<CreateSubjectRequest> { request ->
                        buildValidationResult {
                            condition(
                                request.name.isNotEmpty() && !request.name.onlyDigits(),
                                SubjectErrors.INVALID_SUBJECT_NAME
                            )
                            condition(
                                request.iconName.isNotEmpty(),
                                SubjectErrors.INVALID_SUBJECT_ICON_NAME
                            )
                        }
                    }
                }
                val organizationId: String by inject(named(OrganizationEnv.ORG_ID))
                val requireCapability: RequireCapabilityUseCase by inject()
                val addSubject: AddSubjectUseCase by inject()
                val findAllSubjects: FindAllSubjectsUseCase by inject()

                post {
                    requireCapability(
                        call.jwtPrincipal().payload.claimId,
                        Capability.WriteSubject,
                        organizationId.toUUID()
                    )
                    val body = call.receive<CreateSubjectRequest>()
                    val subjectId = addSubject(body).toString()
                    call.respond(HttpStatusCode.OK, subjectId)
                }
                get {
                    requireCapability(
                        call.jwtPrincipal().payload.claimId,
                        Capability.ReadSubject,
                        organizationId.toUUID()
                    )

                    findAllSubjects().apply {
                        call.respond(HttpStatusCode.OK, this)
                    }
                }
                subjectByIdRoute()
            }
        }
    }
}

fun Route.subjectByIdRoute() {
    route("/{id}") {
        install(RequestValidation) {
            validate<UpdateSubjectRequest> { request ->
                buildValidationResult {
                    request.name.ifPresent {
                        condition(
                            it.isNotEmpty() && !it.onlyDigits(),
                            SubjectErrors.INVALID_SUBJECT_NAME
                        )
                    }
                    request.iconName.ifPresent {
                        condition(
                            it.isNotEmpty(),
                            SubjectErrors.INVALID_SUBJECT_ICON_NAME
                        )
                    }
                }
            }
        }
        val organizationId: UUID by inject(named(OrganizationEnv.ORG_ID))
        val requireCapability: RequireCapabilityUseCase by inject()
        val findSubjectById: FindSubjectByIdUseCase by inject()
        val updateSubject: UpdateSubjectUseCase by inject()
        val removeSubject: RemoveSubjectUseCase by inject()

        get {
            val id = call.parameters["id"]!!.toUUID()

            requireCapability(
                call.jwtPrincipal().payload.claimId,
                Capability.ReadSubject,
                organizationId
            )

            findSubjectById(id).let { subject ->
                call.respond(HttpStatusCode.OK, subject)
            }
        }
        patch {
            val id = call.parameters["id"]!!.toUUID()

            requireCapability(
                call.jwtPrincipal().payload.claimId,
                Capability.WriteSubject,
                organizationId
            )

            val body = call.receive<UpdateSubjectRequest>()
            updateSubject(id, body).let { subject ->
                call.respond(HttpStatusCode.OK, subject)
            }
        }
        delete {
            val id = call.parameters["id"]!!.toUUID()

            requireCapability(
                call.jwtPrincipal().payload.claimId,
                Capability.DeleteSubject,
                organizationId
            )

            removeSubject(id)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}