package com.studiversity.feature.course.work.submission

import com.studiversity.feature.course.element.model.Attachment
import com.studiversity.feature.course.element.model.FileRequest
import com.studiversity.feature.course.work.submission.model.GradeRequest
import com.studiversity.feature.course.work.submission.model.SubmissionErrors
import com.studiversity.feature.course.work.submission.model.SubmissionGrade
import com.studiversity.feature.course.work.submission.usecase.*
import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import com.studiversity.ktor.claimId
import com.studiversity.ktor.getUuid
import com.studiversity.ktor.jwtPrincipal
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.time.Instant

fun Route.workSubmissionRoutes() {
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

            val submissions = findSubmissionsByWork(courseId, call.parameters.getUuid("workId"))
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
        val submitSubmission: SubmitSubmissionUseCase by inject()

        val addFileAttachmentOfSubmission: AddFileAttachmentOfSubmissionUseCase by inject()
        val addLinkAttachmentOfSubmission: AddLinkAttachmentOfSubmissionUseCase by inject()

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
        route("/attachments") {
            val requireSubmissionAuthor: RequireSubmissionAuthorUseCase by inject()
            val isSubmissionAuthor: IsSubmissionAuthorUseCase by inject()
            val findSubmissionAttachments: FindSubmissionAttachmentsUseCase by inject()
            val removeAttachmentOfSubmission: RemoveAttachmentOfSubmissionUseCase by inject()

            post {
                val courseId = call.parameters.getUuid("courseId")
                val workId = call.parameters.getUuid("workId")
                val submissionId = call.parameters.getUuid("submissionId")
                val currentUserId = call.jwtPrincipal().payload.claimId

                requireSubmissionAuthor(submissionId, currentUserId)

                val result: Attachment = when (call.request.queryParameters["upload"]) {
                    "file" -> {
                        call.receiveMultipart().readPart()?.let { part ->
                            if (part is PartData.FileItem) {
                                val fileSourceName = part.originalFileName as String
                                val fileBytes = part.streamProvider().readBytes()
                                val fileName = Instant.now().epochSecond.toString() + "_" + fileSourceName
                                val filePath = "courses/$courseId/elements/$workId/submissions/$submissionId/$fileName"

                                addFileAttachmentOfSubmission(
                                    submissionId = submissionId,
                                    courseId = courseId,
                                    workId = workId,
                                    attachment = FileRequest(fileSourceName, fileBytes, filePath)
                                )
                            } else throw BadRequestException(SubmissionErrors.INVALID_ATTACHMENT)
                        } ?: throw BadRequestException(SubmissionErrors.INVALID_ATTACHMENT)
                    }

                    "link" -> {
                        addLinkAttachmentOfSubmission(submissionId, call.receive())
                    }

                    else -> {
                        throw BadRequestException(SubmissionErrors.INVALID_ATTACHMENT)
                    }
                }
                call.respond(HttpStatusCode.Created, result)
            }
            get {
                val courseId = call.parameters.getUuid("courseId")
                val submissionId = call.parameters.getUuid("submissionId")
                val currentUserId = call.jwtPrincipal().payload.claimId

                if (!isSubmissionAuthor(submissionId, currentUserId))
                    requireCapability(
                        userId = currentUserId,
                        capability = Capability.ReadSubmissions,
                        scopeId = courseId
                    )
                val attachments = findSubmissionAttachments(submissionId)
                call.respond(HttpStatusCode.OK, attachments)
            }
            delete("/{attachmentId}") {
                val courseId = call.parameters.getUuid("courseId")
                val workId = call.parameters.getUuid("workId")
                val submissionId = call.parameters.getUuid("submissionId")
                val attachmentId = call.parameters.getUuid("attachmentId")
                val currentUserId = call.jwtPrincipal().payload.claimId

                requireSubmissionAuthor(submissionId, currentUserId)

                removeAttachmentOfSubmission(
                    courseId = courseId,
                    elementId = workId,
                    submissionId = submissionId,
                    attachmentId = attachmentId
                )
                call.respond(HttpStatusCode.NoContent)
            }
        }
        route("/grade") {
            val setGradeSubmission: SetGradeSubmissionUseCase by inject()
            put {
                val currentUserId = call.jwtPrincipal().payload.claimId
                val courseId = call.parameters.getUuid("courseId")
                val workId = call.parameters.getUuid("workId")
                val submissionId = call.parameters.getUuid("submissionId")

                requireCapability(
                    userId = currentUserId,
                    capability = Capability.GradeSubmission,
                    scopeId = courseId
                )
                val body = call.receive<GradeRequest>()
                setGradeSubmission(workId, SubmissionGrade(body.value, courseId, currentUserId, submissionId))
            }
        }
        post("/submit") {
            val currentUserId = call.jwtPrincipal().payload.claimId
            requireCapability(
                userId = currentUserId,
                capability = Capability.SubmitSubmission,
                scopeId = call.parameters.getUuid("courseId")
            )
            val submittedSubmission = submitSubmission(
                submissionId = call.parameters.getUuid("submissionId"),
                studentId = currentUserId,
            )
            call.respond(HttpStatusCode.OK, submittedSubmission)
        }
        post {

        }
        delete {

        }
    }
}

fun Route.submissionByStudentIdRoute() {
    val requireCapability: RequireCapabilityUseCase by inject()
    val findSubmissionByStudent: FindSubmissionByStudentUseCase by inject()
    get("/{studentId}") {
        val currentUserId = call.jwtPrincipal().payload.claimId
        val submission = findSubmissionByStudent(
            call.parameters.getUuid("workId"),
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