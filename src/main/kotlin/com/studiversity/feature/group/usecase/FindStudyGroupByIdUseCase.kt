package com.studiversity.feature.group.usecase

import com.studiversity.feature.group.StudyGroupRepository
import com.studiversity.feature.group.dto.StudyGroupResponse
import io.ktor.server.plugins.*
import java.util.*

class FindStudyGroupByIdUseCase(private val studyGroupRepository: StudyGroupRepository) {
    operator fun invoke(id: UUID): StudyGroupResponse {
        return studyGroupRepository.findById(id) ?: throw NotFoundException()
    }
}