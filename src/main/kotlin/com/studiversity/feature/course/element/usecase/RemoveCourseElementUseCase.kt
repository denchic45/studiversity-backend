package com.studiversity.feature.course.element.usecase

import com.studiversity.feature.course.element.repository.CourseElementRepository
import com.studiversity.transaction.SuspendTransactionWorker
import io.ktor.server.plugins.*
import java.util.*

class RemoveCourseElementUseCase(
    private val transactionWorker: SuspendTransactionWorker,
    private val courseElementRepository: CourseElementRepository
) {

    suspend operator fun invoke(courseId: UUID, elementId: UUID) = transactionWorker.suspendInvoke {
        if (!courseElementRepository.remove(courseId, elementId)) throw NotFoundException()

    }
}