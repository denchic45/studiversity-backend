package com.studiversity.feature.studygroup.usecase

import com.studiversity.feature.studygroup.StudyGroupErrors
import com.studiversity.feature.studygroup.repository.StudyGroupRepository
import io.ktor.server.plugins.*
import java.util.*

class RemoveStudyGroupUseCase(private val groupRepository: StudyGroupRepository) {
    operator fun invoke(id: UUID) {
        groupRepository.remove(id).apply {
            if (!this)
                throw NotFoundException(StudyGroupErrors.GROUP_DOES_NOT_EXIST)
        }
    }
}