package com.studiversity.feature.course.work.submission.usecase

import com.studiversity.feature.course.element.model.FileAttachment
import com.studiversity.feature.course.element.model.FileRequest
import com.studiversity.feature.course.work.submission.SubmissionRepository
import com.studiversity.transaction.SuspendTransactionWorker
import java.util.*

class AddFileAttachmentOfSubmissionUseCase(
    private val transactionWorker: SuspendTransactionWorker,
    private val submissionRepository: SubmissionRepository
) {
    suspend operator fun invoke(
        submissionId: UUID,
        courseId: UUID,
        workId: UUID,
        attachment: FileRequest
    ): FileAttachment {
        return transactionWorker.suspendInvoke {
            submissionRepository.addSubmissionFileAttachment(
                submissionId = submissionId,
                courseId = courseId,
                workId = workId,
                fileRequest = attachment
            )
        }
    }
}