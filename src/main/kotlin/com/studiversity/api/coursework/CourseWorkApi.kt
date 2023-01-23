package com.studiversity.api.coursework

import com.studiversity.api.util.EmptyResponseResult
import com.studiversity.api.util.ResponseResult
import com.studiversity.api.util.toResult
import com.studiversity.feature.course.element.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import java.io.File
import java.util.*

interface CourseWorkApi {
    suspend fun create(
        courseId: UUID,
        createCourseWorkRequest: CreateCourseWorkRequest
    ): ResponseResult<CourseElementResponse>

    suspend fun getAttachments(
        courseId: UUID,
        courseWorkId: UUID
    ): ResponseResult<List<AttachmentHeader>>

    suspend fun getAttachment(
        courseId: UUID,
        courseWorkId: UUID,
        attachmentId: UUID
    ): ResponseResult<Attachment>

    suspend fun uploadFileToSubmission(
        courseId: UUID,
        courseWorkId: UUID,
        file: File
    ): ResponseResult<FileAttachmentHeader>

    suspend fun addLinkToSubmission(
        courseId: UUID,
        courseWorkId: UUID,
        link: CreateLinkRequest
    ): ResponseResult<LinkAttachmentHeader>

    suspend fun deleteAttachmentOfSubmission(
        courseId: UUID,
        courseWorkId: UUID,
        attachmentId: UUID
    ): EmptyResponseResult
}

class CourseWorkApiImpl(private val client: HttpClient) : CourseWorkApi {
    override suspend fun create(
        courseId: UUID,
        createCourseWorkRequest: CreateCourseWorkRequest
    ): ResponseResult<CourseElementResponse> {
        return client.post("/courses/$courseId/works") {
            contentType(ContentType.Application.Json)
            setBody(createCourseWorkRequest)
        }.toResult()
    }

    override suspend fun getAttachments(
        courseId: UUID,
        courseWorkId: UUID
    ): ResponseResult<List<AttachmentHeader>> {
        return client.get("/courses/${courseId}/works/${courseWorkId}/attachments")
            .toResult()
    }

    override suspend fun getAttachment(
        courseId: UUID,
        courseWorkId: UUID,
        attachmentId: UUID
    ): ResponseResult<Attachment> {
        return client.get("/courses/$courseId/works/$courseWorkId/attachments/$attachmentId").toResult { response ->
            if (response.headers.contains(HttpHeaders.ContentDisposition)) {
                FileAttachment(
                    response.body(),
                    ContentDisposition.parse(response.headers[HttpHeaders.ContentDisposition]!!)
                        .parameter(ContentDisposition.Parameters.FileName)!!

                )
            } else {
                response.body<Link>()
            }
        }
    }

    override suspend fun uploadFileToSubmission(
        courseId: UUID,
        courseWorkId: UUID,
        file: File
    ): ResponseResult<FileAttachmentHeader> =
        client.post("/courses/$courseId/works/$courseWorkId/attachments") {
            parameter("upload", "file")
            contentType(ContentType.MultiPart.FormData)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("file", file.readBytes(), Headers.build {
                            append(HttpHeaders.ContentType, ContentType.defaultForFile(file))
                            append(HttpHeaders.ContentDisposition, "filename=${file.name}")
                        })
                    }
                )
            )
        }.toResult()

    override suspend fun addLinkToSubmission(
        courseId: UUID,
        courseWorkId: UUID,
        link: CreateLinkRequest
    ): ResponseResult<LinkAttachmentHeader> =
        client.post("/courses/$courseId/works/$courseWorkId/attachments") {
            parameter("upload", "link")
            contentType(ContentType.Application.Json)
            setBody(link)
        }.toResult()

    override suspend fun deleteAttachmentOfSubmission(
        courseId: UUID,
        courseWorkId: UUID,
        attachmentId: UUID
    ): EmptyResponseResult {
        return client.delete("/courses/$courseId/works/$courseWorkId/attachments/$attachmentId")
            .toResult()
    }
}