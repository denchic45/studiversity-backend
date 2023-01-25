package com.studiversity.feature.course.element

import com.studiversity.feature.course.element.usecase.*
import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import com.studiversity.ktor.claimId
import com.studiversity.ktor.currentUserId
import com.studiversity.ktor.getUuid
import com.studiversity.ktor.jwtPrincipal
import com.stuiversity.api.course.element.model.SortingCourseElements
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import kotlin.reflect.typeOf


fun Route.courseElementRoutes() {
    route("/elements") {
        val findCourseElementsByCourseId: FindCourseElementsByCourseIdUseCase by inject()
        val requireCapability: RequireCapabilityUseCase by inject()

        get {
            val courseId = call.parameters.getUuid("courseId")
            val sorting = call.parameters.getAll("sort_by")?.map {
                SortingCourseElements.of(it)
                    ?: throw ParameterConversionException(it, typeOf<SortingCourseElements>().toString())
            }

            requireCapability(
                userId = call.currentUserId(),
                capability = Capability.ReadCourseElements,
                scopeId = courseId
            )

            val elements = findCourseElementsByCourseId(courseId, sorting)
            call.respond(HttpStatusCode.OK, elements)
        }
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
                userId = call.currentUserId(),
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