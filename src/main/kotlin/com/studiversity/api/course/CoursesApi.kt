package com.studiversity.api.course

import com.studiversity.api.util.EmptyResponseResult
import com.studiversity.api.util.ResponseResult
import com.studiversity.api.util.toResult
import com.studiversity.feature.course.model.CourseResponse
import com.studiversity.feature.course.model.CreateCourseRequest
import com.studiversity.feature.course.model.UpdateCourseRequest
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.util.*

interface CoursesApi {
    suspend fun create(createCourseRequest: CreateCourseRequest): ResponseResult<CourseResponse>

    suspend fun update(courseId: UUID, updateCourseRequest: UpdateCourseRequest): ResponseResult<CourseResponse>

    suspend fun setArchive(courseId: UUID): EmptyResponseResult

    suspend fun unarchive(courseId: UUID): EmptyResponseResult

    suspend fun delete(courseId: UUID): EmptyResponseResult
}

class CoursesApiImpl(private val client: HttpClient) : CoursesApi {
    override suspend fun create(createCourseRequest: CreateCourseRequest): ResponseResult<CourseResponse> {
        return client.post("/courses") {
            contentType(ContentType.Application.Json)
            setBody(CreateCourseRequest("Test course for submissions"))
        }.toResult()
    }

    override suspend fun update(
        courseId: UUID,
        updateCourseRequest: UpdateCourseRequest
    ): ResponseResult<CourseResponse> {
        return client.patch("/courses/$courseId") {
            contentType(ContentType.Application.Json)
            setBody(updateCourseRequest)
        }.toResult()
    }

    override suspend fun setArchive(courseId: UUID): EmptyResponseResult {
        return client.put("/courses/${courseId}/archived").toResult()
    }

    override suspend fun unarchive(courseId: UUID): EmptyResponseResult {
        return client.delete("/courses/${courseId}/archived").toResult()
    }

    override suspend fun delete(courseId: UUID): EmptyResponseResult {
        return client.delete("/courses/$courseId").toResult()
    }
}