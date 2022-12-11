package com.studiversity.feature.studygroup.usecase

import com.studiversity.feature.studygroup.model.CreateStudyGroupRequest
import com.studiversity.feature.studygroup.repository.StudyGroupRepository
import java.util.*

class AddStudyGroupUseCase(private val groupRepository: StudyGroupRepository) {
    operator fun invoke(request: CreateStudyGroupRequest): UUID {
        return groupRepository.add(request)
    }
}