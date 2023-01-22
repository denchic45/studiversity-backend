package com.studiversity.api.coursework

import com.studiversity.api.util.EmptyResponseResult
import com.studiversity.api.util.ResponseResult
import com.studiversity.api.util.toResult
import com.studiversity.feature.course.element.model.*
import io.ktor.client.*
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
    ): ResponseResult<List<Attachment>>

    suspend fun uploadFileToSubmission(
        courseId: UUID,
        courseWorkId: UUID,
        file: File
    ): ResponseResult<FileAttachment>

    suspend fun addLinkToSubmission(
        courseId: UUID,
        courseWorkId: UUID,
        link: CreateLinkRequest
    ): ResponseResult<LinkAttachment>

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
    ): ResponseResult<List<Attachment>> {
        return client.get("/courses/${courseId}/works/${courseWorkId}/attachments")
            .toResult()
    }

    override suspend fun uploadFileToSubmission(
        courseId: UUID,
        courseWorkId: UUID,
        file: File
    ): ResponseResult<FileAttachment> =
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
    ): ResponseResult<LinkAttachment> =
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