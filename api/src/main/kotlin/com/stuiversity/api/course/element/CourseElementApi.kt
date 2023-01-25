package com.stuiversity.api.course.element

import com.stuiversity.api.course.element.model.CourseElementResponse
import com.stuiversity.api.course.element.model.SortingCourseElements
import com.stuiversity.api.course.element.model.UpdateCourseElementRequest
import com.stuiversity.api.util.EmptyResponseResult
import com.stuiversity.api.util.ResponseResult
import com.stuiversity.api.util.toResult
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.util.*

interface CourseElementApi {

    suspend fun update(
        courseId: UUID,
        elementId: UUID,
        updateCourseElementRequest: UpdateCourseElementRequest
    ): ResponseResult<CourseElementResponse>

    suspend fun getByCourseId(
        courseId: UUID,
        vararg sorting: SortingCourseElements
    ): ResponseResult<List<CourseElementResponse>>

    suspend fun delete(courseId: UUID, elementId: UUID): EmptyResponseResult
}

class CourseElementApiImpl(private val client: HttpClient) : CourseElementApi {
    override suspend fun update(
        courseId: UUID,
        elementId: UUID,
        updateCourseElementRequest: UpdateCourseElementRequest
    ): ResponseResult<CourseElementResponse> {
        return client.patch("/courses/$courseId/elements/$elementId") {
            contentType(ContentType.Application.Json)
            setBody(updateCourseElementRequest)
        }.toResult()
    }

    override suspend fun getByCourseId(
        courseId: UUID,
        vararg sorting: SortingCourseElements
    ): ResponseResult<List<CourseElementResponse>> {
        return client.get("/courses/$courseId/elements") {
            sorting.forEach {
                parameter("sort_by", it.toString())
            }
        }.toResult()
    }

    override suspend fun delete(courseId: UUID, elementId: UUID): EmptyResponseResult {
        return client.delete("/courses/$courseId/elements/$elementId").toResult()
    }
}