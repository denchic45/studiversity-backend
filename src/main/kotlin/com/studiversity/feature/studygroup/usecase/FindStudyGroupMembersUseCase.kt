package com.studiversity.feature.studygroup.usecase

import com.studiversity.feature.studygroup.domain.StudyGroupMembers
import com.studiversity.feature.studygroup.repository.StudyGroupMemberRepository
import java.util.*

class FindStudyGroupMembersUseCase(private val studyGroupMemberRepository: StudyGroupMemberRepository) {
    operator fun invoke(groupId: UUID): StudyGroupMembers {
        return studyGroupMemberRepository.findByGroupId(groupId)
    }
}