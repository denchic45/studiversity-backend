package com.studiversity.feature.course.work.submission.usecase

import com.studiversity.feature.course.element.model.LinkRequest
import com.studiversity.feature.course.work.submission.SubmissionRepository
import com.studiversity.transaction.TransactionWorker
import java.util.*

class AddLinkAttachmentOfSubmissionUseCase(
    private val transactionWorker: TransactionWorker,
    private val submissionRepository: SubmissionRepository
) {
    operator fun invoke(submissionId: UUID, attachment: LinkRequest) = transactionWorker {
        submissionRepository.addSubmissionLinkAttachment(submissionId, attachment)
    }
}