package com.studiversity.feature.course.element.usecase

import com.studiversity.feature.course.element.model.CreateCourseElementRequest
import com.studiversity.feature.course.element.repository.CourseElementRepository
import com.studiversity.transaction.TransactionWorker
import java.util.*

class AddCourseElementUseCase(
    private val transactionWorker: TransactionWorker,
    private val courseElementRepository: CourseElementRepository
) {
    operator fun invoke(courseId: UUID, request: CreateCourseElementRequest) = transactionWorker {
        courseElementRepository.add(courseId, request)
    }
}