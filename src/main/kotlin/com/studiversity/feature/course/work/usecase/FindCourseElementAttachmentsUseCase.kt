package com.studiversity.feature.course.work.usecase

import com.studiversity.feature.attachment.AttachmentRepository
import com.studiversity.transaction.TransactionWorker
import java.util.*

class FindCourseElementAttachmentsUseCase(
    private val transactionWorker: TransactionWorker,
    private val attachmentRepository: AttachmentRepository
) {
    operator fun invoke(submissionId: UUID) = transactionWorker {
        attachmentRepository.findAttachmentsByCourseElementId(submissionId)
    }
}