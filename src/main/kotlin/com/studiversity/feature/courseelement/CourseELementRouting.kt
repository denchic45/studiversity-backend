package com.studiversity.feature.courseelement

import com.studiversity.feature.courseelement.model.CourseElementDetails
import com.studiversity.feature.courseelement.model.CreateCourseElementRequest
import com.studiversity.feature.courseelement.usecase.AddCourseElementUseCase
import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import com.studiversity.ktor.claimId
import com.studiversity.ktor.jwtPrincipal
import com.studiversity.util.toUUID
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.koin.ktor.ext.inject
import kotlin.reflect.KClass

private val createCourseElementCapabilities: Map<KClass<out CourseElementDetails>, Capability> = mapOf(
    CourseElementDetails.Work::class to Capability.WriteCourseWork,
    CourseElementDetails.Post::class to Capability.WriteCoursePost
)

fun Application.courseElementRoutes() {
    routing {
        authenticate("auth-jwt") {
            route("/courses{courseId}/elements") {

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
                    addCourseElement(courseId, body)
                }
                get { }
                courseElementById()
            }
        }
    }
}

fun Route.courseElementById() {
    route("/elementId") {
        get { }
        delete { }
    }
}