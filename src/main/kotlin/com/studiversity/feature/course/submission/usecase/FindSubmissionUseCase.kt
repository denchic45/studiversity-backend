package com.studiversity.feature.course.submission.usecase

import com.studiversity.feature.course.submission.CourseSubmissionRepository
import com.studiversity.transaction.TransactionWorker
import io.ktor.server.plugins.*
import java.util.UUID

class FindSubmissionUseCase(
    private val transactionWorker: TransactionWorker,
    private val submissionRepository: CourseSubmissionRepository
) {

    operator fun invoke(submissionId: UUID) =transactionWorker{
        submissionRepository.find(submissionId) ?: throw NotFoundException()
    }
}