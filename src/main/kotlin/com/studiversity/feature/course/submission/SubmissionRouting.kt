package com.studiversity.feature.course.submission

import com.studiversity.feature.course.submission.usecase.FindSubmissionUseCase
import com.studiversity.feature.course.submission.usecase.FindSubmissionsByWorkUseCase
import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import com.studiversity.ktor.claimId
import com.studiversity.ktor.getUuid
import com.studiversity.ktor.jwtPrincipal
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.courseSubmissionRoutes() {
    route("/submissions") {
        val requireCapability: RequireCapabilityUseCase by inject()
        val findSubmissionsByWork: FindSubmissionsByWorkUseCase by inject()

        get {
            val courseId = call.parameters.getUuid("courseId")
            requireCapability(
                userId = call.jwtPrincipal().payload.claimId,
                capability = Capability.ReadSubmissions,
                scopeId = courseId
            )

            val submissions = findSubmissionsByWork(courseId, call.parameters.getUuid("elementId"))
            call.respond(HttpStatusCode.OK, submissions)
        }
        post { }
        submissionByIdRoute()
    }
}

fun Route.submissionByIdRoute() {
    route("/{submissionId}") {
        val requireCapability: RequireCapabilityUseCase by inject()
        val findSubmission: FindSubmissionUseCase by inject()
        get {
            val currentUserId = call.jwtPrincipal().payload.claimId
            val submission = findSubmission(
                call.parameters.getUuid("submissionId"),
                call.parameters.getUuid("elementId"), currentUserId
            )

            val isOwnSubmission = submission.authorId == currentUserId
            if (isOwnSubmission)
                call.respond(HttpStatusCode.OK, submission)
            else {
                requireCapability(
                    userId = currentUserId,
                    capability = Capability.ReadSubmissions,
                    scopeId = call.parameters.getUuid("courseId")
                )
                call.respond(HttpStatusCode.OK, submission)
            }
        }
        post("/submit") {

        }
    }
}