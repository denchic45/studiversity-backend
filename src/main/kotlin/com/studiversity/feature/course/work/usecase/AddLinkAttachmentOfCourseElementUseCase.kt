package com.studiversity.feature.course.work.usecase

import com.studiversity.feature.attachment.AttachmentRepository
import com.studiversity.feature.course.element.model.LinkAttachment
import com.studiversity.feature.course.element.model.LinkRequest
import com.studiversity.transaction.SuspendTransactionWorker
import java.util.*

class AddLinkAttachmentOfCourseElementUseCase(
    private val transactionWorker: SuspendTransactionWorker,
    private val attachmentRepository: AttachmentRepository
) {
    suspend operator fun invoke(
        elementId: UUID,
        attachment: LinkRequest
    ): LinkAttachment {
        return transactionWorker.suspendInvoke {
            attachmentRepository.addCourseElementLinkAttachment(
                elementId = elementId,
                link = attachment
            )
        }
    }
}