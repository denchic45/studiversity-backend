package com.studiversity.feature.course.submission.usecase

import com.studiversity.feature.course.submission.CourseSubmissionRepository
import com.studiversity.feature.course.submission.model.SubmissionContent
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
        submissionRepository.submitSubmissionContent(submissionId, content) ?: throw NotFoundException()
    }
}