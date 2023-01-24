package com.studiversity.feature.course.element

import com.studiversity.feature.course.element.usecase.FindCourseElementUseCase
import com.studiversity.feature.course.element.usecase.RemoveCourseElementUseCase
import com.studiversity.feature.course.element.usecase.UpdateCourseElementUseCase
import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import com.studiversity.ktor.claimId
import com.studiversity.ktor.getUuid
import com.studiversity.ktor.jwtPrincipal
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject


fun Route.courseElementRoutes() {
    route("/elements") {
        get { }
        courseElementById()
    }
}

fun Route.courseElementById() {
    route("/{elementId}") {
        val requireCapability: RequireCapabilityUseCase by inject()
        val findCourseElement: FindCourseElementUseCase by inject()
        val updateCourseElement: UpdateCourseElementUseCase by inject()
        val removeCourseElement: RemoveCourseElementUseCase by inject()

        get {
            val courseId = call.parameters.getUuid("courseId")

            requireCapability(
                userId = call.jwtPrincipal().payload.claimId,
                capability = Capability.ReadCourseElements,
                scopeId = courseId
            )

            val element = findCourseElement(call.parameters.getUuid("elementId"))
            call.respond(HttpStatusCode.OK, element)

        }
        patch {
            val courseId = call.parameters.getUuid("courseId")
            val elementId = call.parameters.getUuid("elementId")

            requireCapability(
                userId = call.jwtPrincipal().payload.claimId,
                capability = Capability.WriteCourse,
                scopeId = courseId
            )

            val element = updateCourseElement(courseId, elementId, call.receive())
            call.respond(HttpStatusCode.OK, element)
        }
        delete {
            val currentUserId = call.jwtPrincipal().payload.claimId
            val courseId = call.parameters.getUuid("courseId")
            val workId = call.parameters.getUuid("elementId")

            requireCapability(
                userId = currentUserId,
                capability = Capability.DeleteCourseElements,
                scopeId = courseId
            )
            removeCourseElement(courseId, workId)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}