package com.studiversity.feature.course.element.usecase

import com.studiversity.feature.attachment.AttachmentRepository
import com.studiversity.feature.course.element.repository.CourseElementRepository
import com.studiversity.transaction.SuspendTransactionWorker
import com.stuiversity.api.course.element.model.CourseElementType
import io.ktor.server.plugins.*
import java.util.*

class RemoveCourseElementUseCase(
    private val transactionWorker: SuspendTransactionWorker,
    private val courseElementRepository: CourseElementRepository,
    private val attachmentRepository: AttachmentRepository
) {

    suspend operator fun invoke(courseId: UUID, elementId: UUID) = transactionWorker.suspendInvoke {
        when (courseElementRepository.findTypeByElementId(elementId)) {
            CourseElementType.WORK -> {
                attachmentRepository.removeAllByCourseWorkId(courseId, elementId)
                courseElementRepository.remove(courseId, elementId)
            }

            CourseElementType.MATERIAL -> TODO()
            null -> throw NotFoundException()
        }
    }
}