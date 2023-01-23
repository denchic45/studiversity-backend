package com.studiversity.feature.course.element.usecase

import com.studiversity.feature.attachment.AttachmentRepository
import com.studiversity.feature.course.element.model.CreateFileRequest
import com.studiversity.feature.course.element.model.FileAttachmentHeader
import com.studiversity.transaction.SuspendTransactionWorker
import java.util.*

class AddFileAttachmentOfCourseElementUseCase(
    private val transactionWorker: SuspendTransactionWorker,
    private val attachmentRepository: AttachmentRepository
) {
    suspend operator fun invoke(
        elementId: UUID,
        courseId: UUID,
        attachment: CreateFileRequest
    ): FileAttachmentHeader {
        return transactionWorker.suspendInvoke {
            attachmentRepository.addCourseElementFileAttachment(
                elementId = elementId,
                courseId = courseId,
                file = attachment
            )
        }
    }
}