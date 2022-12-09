package com.studiversity.feature.group.usecase

import com.studiversity.feature.group.StudyGroupRepository
import com.studiversity.feature.group.dto.CreateStudyGroupRequest

class AddStudyGroupUseCase(private val groupRepository: StudyGroupRepository) {
    operator fun invoke(request: CreateStudyGroupRequest) {
        groupRepository.add(request)
    }
}