package com.studiversity.feature.course.submission.usecase

import com.studiversity.feature.course.submission.CourseSubmissionRepository
import com.studiversity.feature.course.submission.model.SubmissionContent
import com.studiversity.transaction.TransactionWorker
import io.ktor.server.plugins.*
import java.util.*

class UpdateSubmissionContentUseCase(
    private val transactionWorker: TransactionWorker,
    private val submissionRepository: CourseSubmissionRepository
) {

    operator fun invoke(submissionId: UUID, content: SubmissionContent?) = transactionWorker {
        submissionRepository.updateSubmissionContent(submissionId, content) ?: throw NotFoundException()
    }
}