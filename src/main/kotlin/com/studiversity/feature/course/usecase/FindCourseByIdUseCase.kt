package com.studiversity.feature.course.usecase

import com.studiversity.feature.course.model.CourseResponse
import com.studiversity.feature.course.repository.CourseRepository
import io.ktor.server.plugins.*
import java.util.*

class FindCourseByIdUseCase(private val courseRepository: CourseRepository) {
    operator fun invoke(id: UUID): CourseResponse {
        return courseRepository.findById(id) ?: throw NotFoundException()
    }
}