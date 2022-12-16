package com.studiversity.feature.course

import com.studiversity.Constants
import com.studiversity.feature.course.model.CreateCourseRequest
import com.studiversity.feature.course.usecase.AddCourseUseCase
import com.studiversity.feature.course.usecase.FindCourseByIdUseCase
import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import com.studiversity.feature.studygroup.StudyGroupErrors
import com.studiversity.ktor.claimId
import com.studiversity.ktor.jwtPrincipal
import com.studiversity.util.onlyDigits
import com.studiversity.util.toUUID
import com.studiversity.validation.buildValidationResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.courseRoutes() {
    routing {
        authenticate("auth-jwt") {
            route("/courses") {

                install(RequestValidation) {
                    validate<CreateCourseRequest> { request ->
                        buildValidationResult {
                            condition(
                                request.name.isEmpty() || request.name.onlyDigits(),
                                StudyGroupErrors.INVALID_GROUP_NAME
                            )
                        }
                    }
                }

                val requireCapability: RequireCapabilityUseCase by inject()
                val addCourse: AddCourseUseCase by inject()

                post {
                    val currentUserId = call.principal<JWTPrincipal>()!!.payload.getClaim("sub").asString().toUUID()

                    requireCapability(currentUserId, Capability.WriteCourses, Constants.organizationId)

                    val body = call.receive<CreateCourseRequest>()

                    val courseId = addCourse(body).toString()
                    call.respond(HttpStatusCode.OK, courseId)
                }
                courseByIdRoutes()
            }
        }
    }
}

fun Route.courseByIdRoutes() {
    route("/{id}") {
        val requireCapability: RequireCapabilityUseCase by inject()
        val findCourseById: FindCourseByIdUseCase by inject()

        get {
            val id = call.parameters["id"]!!.toUUID()

            val currentUserId = call.jwtPrincipal().payload.claimId

            requireCapability(currentUserId, Capability.ReadGroup, id)

            findCourseById(id).let { course -> call.respond(HttpStatusCode.OK, course) }
        }
        courseMembersRoute()
    }
}

fun Route.courseMembersRoute() {
    route("/members") {
        get {

        }
    }
}
