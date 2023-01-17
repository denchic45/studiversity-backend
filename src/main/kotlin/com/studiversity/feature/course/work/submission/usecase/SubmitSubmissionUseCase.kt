package com.studiversity.feature.course.work.submission.usecase

import com.studiversity.feature.course.work.submission.CourseSubmissionRepository
import com.studiversity.feature.course.work.submission.model.SubmissionContent
import com.studiversity.feature.course.work.submission.model.SubmissionState
import com.studiversity.transaction.TransactionWorker
import io.ktor.server.plugins.*
import java.util.*

class SubmitSubmissionUseCase(
    private val transactionWorker: TransactionWorker,
    private val submissionRepository: CourseSubmissionRepository
) {

    operator fun invoke(submissionId: UUID, studentId: UUID, content: SubmissionContent?) = transactionWorker {
        val currentSubmission = submissionRepository.find(submissionId) ?: throw NotFoundException()
        if (currentSubmission.authorId != studentId)
            throw BadRequestException("INVALID_AUTHOR")
        if (currentSubmission.state == SubmissionState.SUBMITTED)
            throw BadRequestException("ALREADY_SUBMITTED")
        submissionRepository.submitSubmissionContent(submissionId, content)
    }
}