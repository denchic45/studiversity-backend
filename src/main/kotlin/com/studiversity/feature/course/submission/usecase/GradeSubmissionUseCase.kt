package com.studiversity.feature.course.submission.usecase

import com.studiversity.feature.course.element.repository.CourseElementRepository
import com.studiversity.feature.course.submission.CourseSubmissionRepository
import com.studiversity.transaction.TransactionWorker
import io.ktor.server.plugins.*
import java.util.*

class GradeSubmissionUseCase(
    private val transactionWorker: TransactionWorker,
    private val submissionRepository: CourseSubmissionRepository,
    private val courseElementRepository: CourseElementRepository
) {

    operator fun invoke(submissionId: UUID, workId:UUID,grade: Short, gradedBy: UUID) = transactionWorker {
//        val currentSubmission = submissionRepository.find(submissionId) ?: throw NotFoundException()
        if (courseElementRepository.findMaxGradeByWorkId(workId) < grade)
            throw BadRequestException("MAX_GRADE_LIMIT")
        submissionRepository.setGradeSubmission(submissionId, grade, gradedBy)
    }
}