package com.studiversity.feature.course.usecase

import com.studiversity.feature.course.model.CreateCourseRequest
import com.studiversity.feature.course.repository.CourseRepository
import java.util.*

class AddCourseUseCase(private val courseRepository: CourseRepository) {
    operator fun invoke(request: CreateCourseRequest): UUID {
        return courseRepository.add(request)
    }
}