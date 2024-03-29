package com.studiversity.feature.studygroup.usecase

import com.stuiversity.api.studygroup.model.StudyGroupResponse
import com.studiversity.feature.studygroup.repository.StudyGroupRepository
import com.studiversity.transaction.TransactionWorker
import io.ktor.server.plugins.*
import java.util.*

class FindStudyGroupByIdUseCase(
    private val transactionWorker: TransactionWorker,
    private val studyGroupRepository: StudyGroupRepository
) {
    operator fun invoke(id: UUID): StudyGroupResponse = transactionWorker {
        studyGroupRepository.findById(id) ?: throw NotFoundException()
    }
}