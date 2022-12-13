package com.studiversity.feature.studygroup.usecase

import com.studiversity.feature.studygroup.model.UpdateStudyGroupMemberRequest
import com.studiversity.feature.studygroup.repository.StudyGroupMemberRepository
import java.util.*

class UpdateStudyGroupMemberUseCase(private val studyGroupMemberRepository: StudyGroupMemberRepository) {
    operator fun invoke(groupId: UUID, memberId: UUID, updateStudyGroupMemberRequest: UpdateStudyGroupMemberRequest) {
        studyGroupMemberRepository.update(groupId,memberId,updateStudyGroupMemberRequest)
    }
}