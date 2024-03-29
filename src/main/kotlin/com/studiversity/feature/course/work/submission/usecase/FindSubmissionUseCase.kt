package com.studiversity.feature.course.work.submission.usecase

import com.studiversity.feature.course.work.submission.SubmissionRepository
import com.stuiversity.api.course.work.submission.model.SubmissionState
import com.studiversity.transaction.TransactionWorker
import io.ktor.server.plugins.*
import java.util.*

class FindSubmissionUseCase(
    private val transactionWorker: TransactionWorker,
    private val submissionRepository: SubmissionRepository
) {

    operator fun invoke(submissionId: UUID, receivingUserId: UUID) = transactionWorker {
        submissionRepository.find(submissionId)?.let { response ->
            if (response.state == SubmissionState.NEW && response.authorId == receivingUserId) {
                submissionRepository.updateSubmissionState(submissionId, SubmissionState.CREATED)
                submissionRepository.find(submissionId)!!
            } else response
        } ?: throw NotFoundException()
    }
}