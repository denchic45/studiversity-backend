package com.studiversity.feature.course.work.usecase

import com.studiversity.feature.attachment.AttachmentRepository
import com.studiversity.feature.course.element.model.FileAttachment
import com.studiversity.feature.course.element.model.FileRequest
import com.studiversity.transaction.SuspendTransactionWorker
import java.util.*

class AddFileAttachmentOfCourseElementUseCase(
    private val transactionWorker: SuspendTransactionWorker,
    private val attachmentRepository: AttachmentRepository
) {
    suspend operator fun invoke(
        elementId: UUID,
        courseId: UUID,
        attachment: FileRequest
    ): FileAttachment {
        return transactionWorker.suspendInvoke {
            attachmentRepository.addCourseElementFileAttachment(
                elementId = elementId,
                courseId = courseId,
                file = attachment
            )
        }
    }
}