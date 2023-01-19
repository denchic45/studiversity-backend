package com.studiversity.feature.course.work.submission.usecase

import com.studiversity.feature.attachment.AttachmentRepository
import com.studiversity.feature.course.element.model.LinkRequest
import com.studiversity.transaction.TransactionWorker
import java.util.*

class AddLinkAttachmentOfSubmissionUseCase(
    private val transactionWorker: TransactionWorker,
    private val attachmentRepository: AttachmentRepository
) {
    operator fun invoke(submissionId: UUID, attachment: LinkRequest) = transactionWorker {
        attachmentRepository.addSubmissionLinkAttachment(submissionId, attachment)
    }
}