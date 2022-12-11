package com.studiversity.feature.studygroup.usecase

import com.studiversity.feature.studygroup.model.StudyGroupResponse
import com.studiversity.feature.studygroup.repository.StudyGroupRepository
import io.ktor.server.plugins.*
import java.util.*

class FindStudyGroupByIdUseCase(private val studyGroupRepository: StudyGroupRepository) {
    operator fun invoke(id: UUID): StudyGroupResponse {
        return studyGroupRepository.findById(id) ?: throw NotFoundException()
    }
}