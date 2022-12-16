package com.studiversity.feature.course.usecase

import com.studiversity.feature.course.model.CourseResponse
import com.studiversity.feature.course.model.UpdateCourseRequest
import com.studiversity.feature.course.repository.CourseRepository
import io.ktor.server.plugins.*
import java.util.*

class UpdateCourseUseCase(private val courseRepository: CourseRepository) {
    operator fun invoke(id: UUID, request: UpdateCourseRequest): CourseResponse {
        return courseRepository.update(id, request) ?: throw NotFoundException()
    }
}