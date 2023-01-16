package com.studiversity.feature.course.submission.usecase

import com.studiversity.feature.course.element.model.CreateFileAttachmentRequest
import com.studiversity.feature.course.submission.CourseSubmissionRepository
import com.studiversity.transaction.TransactionWorker
import java.util.*

class AddFileAttachmentOfSubmissionUseCase(
    private val transactionWorker: TransactionWorker,
    private val courseSubmissionRepository: CourseSubmissionRepository
) {
    operator fun invoke(submissionId: UUID, attachments: List<CreateFileAttachmentRequest>) = transactionWorker {
        courseSubmissionRepository.addSubmissionAttachments(submissionId, attachments)
    }
}