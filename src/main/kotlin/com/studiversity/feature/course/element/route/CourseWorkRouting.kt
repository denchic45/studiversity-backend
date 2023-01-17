package com.studiversity.feature.course.element.route

import com.studiversity.feature.course.element.model.CreateCourseWorkRequest
import com.studiversity.feature.course.element.usecase.FindCourseElementUseCase
import com.studiversity.feature.course.work.submission.workSubmissionRoutes
import com.studiversity.feature.course.work.usecase.AddCourseWorkUseCase
import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import com.studiversity.ktor.claimId
import com.studiversity.ktor.jwtPrincipal
import com.studiversity.util.toUUID
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.koin.ktor.ext.inject

fun Route.courseWorksRoutes() {
    route("/works") {
        val requireCapability: RequireCapabilityUseCase by inject()
        val addCourseWork: AddCourseWorkUseCase by inject()
        post {
            val body: CreateCourseWorkRequest = call.receive()
            val courseId = call.parameters.getOrFail("courseId").toUUID()
            requireCapability(
                userId = call.jwtPrincipal().payload.claimId,
                capability = Capability.WriteCourseWork,
                scopeId = courseId
            )
            addCourseWork(courseId, body).let { courseElement ->
                call.respond(courseElement)
            }
        }
        courseWorkById()
    }
}

fun Route.courseWorkById() {
    route("/{workId}") {
        val requireCapability: RequireCapabilityUseCase by inject()
        val findCourseElement: FindCourseElementUseCase by inject()

        get {
            val courseId = call.parameters.getOrFail("courseId").toUUID()

            requireCapability(
                userId = call.jwtPrincipal().payload.claimId,
                capability = Capability.ReadCourseElements,
                scopeId = courseId
            )

            val element = findCourseElement(call.parameters.getOrFail("elementId").toUUID())
            call.respond(HttpStatusCode.OK, element)
        }
        workSubmissionRoutes()
    }
}