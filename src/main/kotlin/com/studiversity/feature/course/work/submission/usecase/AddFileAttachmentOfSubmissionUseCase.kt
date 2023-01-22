package com.studiversity.feature.course.work.submission.usecase

import com.studiversity.feature.attachment.AttachmentRepository
import com.studiversity.feature.course.element.model.CreateFileRequest
import com.studiversity.feature.course.element.model.FileAttachment
import com.studiversity.transaction.SuspendTransactionWorker
import java.util.*

class AddFileAttachmentOfSubmissionUseCase(
    private val transactionWorker: SuspendTransactionWorker,
    private val attachmentRepository: AttachmentRepository
) {
    suspend operator fun invoke(
        submissionId: UUID,
        courseId: UUID,
        workId: UUID,
        attachment: CreateFileRequest
    ): FileAttachment {
        return transactionWorker.suspendInvoke {
            attachmentRepository.addSubmissionFileAttachment(
                submissionId = submissionId,
                courseId = courseId,
                workId = workId,
                createFileRequest = attachment
            )
        }
    }
}