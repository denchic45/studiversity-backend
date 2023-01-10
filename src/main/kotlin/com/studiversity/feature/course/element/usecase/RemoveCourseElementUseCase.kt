package com.studiversity.feature.course.element.usecase

import com.studiversity.feature.course.element.repository.CourseElementRepository
import com.studiversity.transaction.TransactionWorker
import io.ktor.server.plugins.*
import java.util.*

class RemoveCourseElementUseCase(
    private val transactionWorker: TransactionWorker,
    private val courseElementRepository: CourseElementRepository
) {

    operator fun invoke(elementId: UUID) = transactionWorker {
        if (!courseElementRepository.remove(elementId)) throw NotFoundException()
    }
}