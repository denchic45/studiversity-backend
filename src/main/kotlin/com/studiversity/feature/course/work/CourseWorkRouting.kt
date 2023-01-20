package com.studiversity.feature.course.work

import com.studiversity.feature.course.element.model.Attachment
import com.studiversity.feature.course.element.model.CreateCourseWorkRequest
import com.studiversity.feature.course.element.model.FileRequest
import com.studiversity.feature.course.element.usecase.FindCourseElementUseCase
import com.studiversity.feature.course.work.submission.model.SubmissionErrors
import com.studiversity.feature.course.work.submission.workSubmissionRoutes
import com.studiversity.feature.course.work.usecase.AddCourseWorkUseCase
import com.studiversity.feature.course.work.usecase.AddFileAttachmentOfCourseElementUseCase
import com.studiversity.feature.course.work.usecase.AddLinkAttachmentOfCourseElementUseCase
import com.studiversity.feature.role.Capability
import com.studiversity.feature.role.usecase.RequireCapabilityUseCase
import com.studiversity.ktor.claimId
import com.studiversity.ktor.getUuid
import com.studiversity.ktor.jwtPrincipal
import com.studiversity.util.toUUID
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import org.koin.ktor.ext.inject
import java.time.Instant

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
        route("/attachments") {

            val addFileAttachmentOfCourseElement: AddFileAttachmentOfCourseElementUseCase by inject()
            val addLinkAttachmentOfCourseElement: AddLinkAttachmentOfCourseElementUseCase by inject()

            get {

            }
            post {
                val courseId = call.parameters.getUuid("courseId")
                val workId = call.parameters.getUuid("workId")
                val currentUserId = call.jwtPrincipal().payload.claimId

                requireCapability(
                    userId = currentUserId,
                    capability = Capability.WriteCourseWork,
                    scopeId = courseId
                )

                val result: Attachment = when (call.request.queryParameters["upload"]) {
                    "file" -> {
                        call.receiveMultipart().readPart()?.let { part ->
                            if (part is PartData.FileItem) {
                                val fileSourceName = part.originalFileName as String
                                val fileBytes = part.streamProvider().readBytes()
                                val fileName = Instant.now().epochSecond.toString() + "_" + fileSourceName
                                val filePath = "courses/$courseId/elements/$workId/$fileName"

                                addFileAttachmentOfCourseElement(
                                    elementId = workId,
                                    courseId = courseId,
                                    attachment = FileRequest(fileSourceName, fileBytes, filePath)
                                )
                            } else throw BadRequestException(SubmissionErrors.INVALID_ATTACHMENT)
                        } ?: throw BadRequestException(SubmissionErrors.INVALID_ATTACHMENT)
                    }

                    "link" -> {
                        addLinkAttachmentOfCourseElement(workId, call.receive())
                    }

                    else -> {
                        throw BadRequestException(SubmissionErrors.INVALID_ATTACHMENT)
                    }
                }
                call.respond(HttpStatusCode.Created, result)
            }
            delete("/{attachmentId}") { }
        }
        workSubmissionRoutes()
    }
}