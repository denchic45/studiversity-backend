package com.studiversity.feature.course.work.submission.usecase

import com.studiversity.feature.course.work.submission.SubmissionRepository
import com.studiversity.transaction.TransactionWorker
import java.util.*

class FindSubmissionAttachmentsUseCase(
    private val transactionWorker: TransactionWorker,
    private val submissionRepository: SubmissionRepository
) {
    operator fun invoke(submissionId: UUID) = transactionWorker {
        submissionRepository.findAttachmentsBySubmissionId(submissionId)
    }
}