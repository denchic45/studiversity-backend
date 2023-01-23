package com.studiversity.api.submission

import com.studiversity.api.util.EmptyResponseResult
import com.studiversity.api.util.ResponseResult
import com.studiversity.api.util.toResult
import com.studiversity.feature.course.element.model.AttachmentHeader
import com.studiversity.feature.course.element.model.CreateLinkRequest
import com.studiversity.feature.course.element.model.FileAttachmentHeader
import com.studiversity.feature.course.element.model.LinkAttachmentHeader
import com.studiversity.feature.course.work.submission.model.GradeRequest
import com.studiversity.feature.course.work.submission.model.SubmissionResponse
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import java.io.File
import java.util.*

interface SubmissionsApi {
    suspend fun getAllByCourseWorkId(
        courseId: UUID,
        courseWorkId: UUID
    ): ResponseResult<List<SubmissionResponse>>

    suspend fun getByStudent(
        courseId: UUID,
        courseWorkId: UUID,
        userId: UUID
    ): ResponseResult<SubmissionResponse>

    suspend fun getById(
        courseId: UUID,
        courseWorkId: UUID, submissionId: UUID
    ): ResponseResult<SubmissionResponse>

    suspend fun gradeSubmission(
        courseId: UUID,
        courseWorkId: UUID,
        submissionId: UUID,
        grade: Short
    ): ResponseResult<SubmissionResponse>

    suspend fun getAttachments(
        courseId: UUID,
        courseWorkId: UUID,
        submissionId: UUID
    ): ResponseResult<List<AttachmentHeader>>

    suspend fun uploadFileToSubmission(
        courseId: UUID,
        courseWorkId: UUID,
        submissionId: UUID,
        file: File
    ): ResponseResult<FileAttachmentHeader>

    suspend fun addLinkToSubmission(
        courseId: UUID,
        courseWorkId: UUID,
        submissionId: UUID,
        link: CreateLinkRequest
    ): ResponseResult<LinkAttachmentHeader>

    suspend fun deleteAttachmentOfSubmission(
        courseId: UUID,
        courseWorkId: UUID,
        submissionId: UUID,
        attachmentId: UUID
    ): EmptyResponseResult

    suspend fun submitSubmission(
        courseId: UUID,
        courseWorkId: UUID,
        submissionId: UUID
    ): ResponseResult<SubmissionResponse>
}

class SubmissionsApiImpl(private val client: HttpClient) : SubmissionsApi {
    override suspend fun getAllByCourseWorkId(
        courseId: UUID,
        courseWorkId: UUID
    ): ResponseResult<List<SubmissionResponse>> {
        return client.get("/courses/${courseId}/works/${courseWorkId}/submissions").toResult()
    }

    override suspend fun getByStudent(
        courseId: UUID,
        courseWorkId: UUID, userId: UUID
    ): ResponseResult<SubmissionResponse> {
        return client.get("/courses/$courseId/works/$courseWorkId/submissionsByStudentId/${userId}")
            .toResult()
    }

    override suspend fun getById(
        courseId: UUID,
        courseWorkId: UUID,
        submissionId: UUID
    ): ResponseResult<SubmissionResponse> {
        return client.get("/courses/$courseId/works/$courseWorkId/submissions/$submissionId")
            .toResult()
    }

    override suspend fun gradeSubmission(
        courseId: UUID,
        courseWorkId: UUID,
        submissionId: UUID,
        grade: Short,
    ): ResponseResult<SubmissionResponse> {
        return client.put("/courses/${courseId}/works/${courseWorkId}/submissions/${submissionId}/grade") {
            contentType(ContentType.Application.Json)
            setBody(GradeRequest(grade))
        }.toResult()
    }

    override suspend fun getAttachments(
        courseId: UUID,
        courseWorkId: UUID,
        submissionId: UUID
    ): ResponseResult<List<AttachmentHeader>> {
        return client.get("/courses/${courseId}/works/${courseWorkId}/submissions/${submissionId}/attachments")
            .toResult()
    }

    override suspend fun uploadFileToSubmission(
        courseId: UUID,
        courseWorkId: UUID,
        submissionId: UUID,
        file: File
    ): ResponseResult<FileAttachmentHeader> =
        client.post("/courses/$courseId/works/$courseWorkId/submissions/${submissionId}/attachments") {
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
        submissionId: UUID,
        link: CreateLinkRequest
    ): ResponseResult<LinkAttachmentHeader> =
        client.post("/courses/$courseId/works/$courseWorkId/submissions/${submissionId}/attachments") {
            parameter("upload", "link")
            contentType(ContentType.Application.Json)
            setBody(link)
        }.toResult()

    override suspend fun deleteAttachmentOfSubmission(
        courseId: UUID,
        courseWorkId: UUID,
        submissionId: UUID,
        attachmentId: UUID
    ): EmptyResponseResult {
        return client.delete("/courses/$courseId/works/$courseWorkId/submissions/${submissionId}/attachments/$attachmentId")
            .toResult()
    }

    override suspend fun submitSubmission(
        courseId: UUID,
        courseWorkId: UUID,
        submissionId: UUID
    ): ResponseResult<SubmissionResponse> {
        return client.post("/courses/$courseId/works/$courseWorkId/submissions/${submissionId}/submit").toResult()
    }
}