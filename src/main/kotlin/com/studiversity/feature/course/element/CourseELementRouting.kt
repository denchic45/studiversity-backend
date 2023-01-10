package com.studiversity.feature.course.element

import com.studiversity.feature.course.element.model.CourseElementDetails
import com.studiversity.feature.course.element.model.CreateCourseElementRequest
import com.studiversity.feature.course.element.usecase.AddCourseElementUseCase
import com.studiversity.feature.course.element.usecase.FindCourseElementUseCase
import com.studiversity.feature.course.element.usecase.RemoveCourseElementUseCase
import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import com.studiversity.ktor.claimId
import com.studiversity.ktor.jwtPrincipal
import com.studiversity.util.toUUID
import io.github.jan.supabase.storage.Storage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.koin.ktor.ext.inject
import kotlin.reflect.KClass

private val createCourseElementCapabilities: Map<KClass<out CourseElementDetails>, Capability> = mapOf(
    CourseElementDetails.Work::class to Capability.WriteCourseAssignment,
    CourseElementDetails.Post::class to Capability.WriteCoursePost
)

fun Application.courseElementRoutes() {
    routing {
        authenticate("auth-jwt") {
            route("/courses/{courseId}/elements") {

                val requireCapability: RequireCapabilityUseCase by inject()
                val addCourseElement: AddCourseElementUseCase by inject()
                post {
                    val body: CreateCourseElementRequest = call.receive()
                    val courseId = call.parameters.getOrFail("courseId").toUUID()
                    requireCapability(
                        userId = call.jwtPrincipal().payload.claimId,
                        capability = createCourseElementCapabilities.getValue(body.details::class),
                        scopeId = courseId
                    )
                    addCourseElement(courseId, body).let { courseElement ->
                        call.respond(courseElement)
                    }
                }
                get {

                }
                courseElementById()
            }
        }
    }
}

fun Route.courseElementById() {
    route("/{elementId}") {
        val requireCapability: RequireCapabilityUseCase by inject()
        val findCourseElement: FindCourseElementUseCase by inject()
        val removeCourseElement: RemoveCourseElementUseCase by inject()

        val storage: Storage by inject()

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
        delete {
            val courseId = call.parameters.getOrFail("courseId").toUUID()

            requireCapability(
                userId = call.jwtPrincipal().payload.claimId,
                capability = Capability.DeleteCourseElements,
                scopeId = courseId
            )

            removeCourseElement(call.parameters.getOrFail("elementId").toUUID())
            call.respond(HttpStatusCode.NoContent)
        }
    }
}