package com.studiversity.feature.course.work.submission.usecase

import com.studiversity.feature.course.element.model.FileAttachment
import com.studiversity.feature.course.work.submission.CourseSubmissionRepository
import com.studiversity.transaction.TransactionWorker
import java.util.*

class AddFileAttachmentOfSubmissionUseCase(
    private val transactionWorker: TransactionWorker,
    private val courseSubmissionRepository: CourseSubmissionRepository
) {
    operator fun invoke(submissionId: UUID, attachments: List<FileAttachment>) = transactionWorker {
        courseSubmissionRepository.addSubmissionAttachments(submissionId, attachments)
    }
}