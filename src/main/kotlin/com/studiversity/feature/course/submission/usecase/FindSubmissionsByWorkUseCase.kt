package com.studiversity.feature.course.submission.usecase

import com.studiversity.feature.course.submission.CourseSubmissionRepository
import com.studiversity.transaction.TransactionWorker
import io.ktor.server.plugins.*
import java.util.*

class FindSubmissionsByWorkUseCase(
    private val transactionWorker: TransactionWorker,
    private val submissionRepository: CourseSubmissionRepository
) {

    operator fun invoke(courseId: UUID, courseWorkId: UUID) = transactionWorker {
        submissionRepository.findByWorkId(courseId, courseWorkId)
    }
}