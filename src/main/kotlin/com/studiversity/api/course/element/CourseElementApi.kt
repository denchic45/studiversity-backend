package com.studiversity.api.course.element

import com.studiversity.api.util.EmptyResponseResult
import com.studiversity.api.util.toResult
import io.ktor.client.*
import io.ktor.client.request.*
import java.util.*

interface CourseElementApi {
    suspend fun delete(courseId: UUID, elementId: UUID): EmptyResponseResult
}

class CourseElementApiImpl(private val client: HttpClient) : CourseElementApi {
    override suspend fun delete(courseId: UUID, elementId: UUID): EmptyResponseResult {
        return client.delete("/courses/$courseId/elements/$elementId").toResult()
    }
}