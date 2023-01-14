package com.studiversity.feature.course.submission

import com.studiversity.feature.course.submission.model.UpdateSubmissionRequest
import com.studiversity.feature.course.submission.usecase.*
import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import com.studiversity.ktor.claimId
import com.studiversity.ktor.getUuid
import com.studiversity.ktor.jwtPrincipal
import com.studiversity.util.presentOrElse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
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
    route("/submissionsByStudentId") {
        submissionByStudentIdRoute()
    }
}

fun Route.submissionByIdRoute() {
    route("/{submissionId}") {
        val requireCapability: RequireCapabilityUseCase by inject()
        val findSubmission: FindSubmissionUseCase by inject()
        val updateSubmissionContent: UpdateSubmissionContentUseCase by inject()
        val submitSubmission: SubmitSubmissionUseCase by inject()
        val gradeSubmission: GradeSubmissionUseCase by inject()


        get {
            val currentUserId = call.jwtPrincipal().payload.claimId
            val submission = findSubmission(
                call.parameters.getUuid("submissionId"),
                currentUserId
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
        patch {
            val currentUserId = call.jwtPrincipal().payload.claimId
            val body = call.receive<UpdateSubmissionRequest>()

            val courseId = call.parameters.getUuid("courseId")
            val workId = call.parameters.getUuid("elementId")
            val submissionId = call.parameters.getUuid("submissionId")

            body.content.ifPresentSuspended {
                requireCapability(
                    userId = currentUserId,
                    capability = Capability.SubmitSubmission,
                    scopeId = courseId
                )
                val updatedSubmission = updateSubmissionContent(
                    submissionId,
                    it
                )
                call.respond(HttpStatusCode.OK, updatedSubmission)
            }
            body.grade.ifPresentSuspended {
                requireCapability(
                    userId = currentUserId,
                    capability = Capability.GradeSubmission,
                    scopeId = courseId
                )
                val updatedSubmission = gradeSubmission(
                    submissionId,
                    workId,
                    it,
                    currentUserId
                )
                call.respond(HttpStatusCode.OK, updatedSubmission)
            }
        }
        post("/submit") {
            val currentUserId = call.jwtPrincipal().payload.claimId
            val body = call.receive<UpdateSubmissionRequest>()
            val content = body.content.presentOrElse { throw BadRequestException("CONTENT_NEEDED") }
            requireCapability(
                userId = currentUserId,
                capability = Capability.SubmitSubmission,
                scopeId = call.parameters.getUuid("courseId")
            )
            val submittedSubmission = submitSubmission(
                submissionId = call.parameters.getUuid("submissionId"),
                studentId = currentUserId,
                content = content
            )
            call.respond(HttpStatusCode.OK, submittedSubmission)
        }
    }
}

fun Route.submissionByStudentIdRoute() {
    val requireCapability: RequireCapabilityUseCase by inject()
    val findSubmissionByStudent: FindSubmissionByStudentUseCase by inject()
    get("/{studentId}") {
        val currentUserId = call.jwtPrincipal().payload.claimId
        val submission = findSubmissionByStudent(
            call.parameters.getUuid("elementId"),
            call.parameters.getUuid("studentId"),
            currentUserId
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
}