package com.studiversity.feature.courseelement.usecase

import com.studiversity.feature.courseelement.model.CreateCourseElementRequest
import com.studiversity.feature.courseelement.repository.CourseElementRepository
import com.studiversity.transaction.TransactionWorker
import java.util.UUID

class AddCourseElementUseCase(
    private val transactionWorker: TransactionWorker,
    private val courseElementRepository: CourseElementRepository
) {
    operator fun invoke(courseId:UUID, request:CreateCourseElementRequest) = transactionWorker {
        courseElementRepository.add(courseId, request)
    }
}