package com.studiversity.feature.studygroup.usecase

import com.studiversity.feature.studygroup.model.EnrolStudyGroupMemberRequest
import com.studiversity.feature.studygroup.repository.StudyGroupMemberRepository
import java.util.*

class EnrollStudyGroupMemberUseCase(
    private val studyGroupMemberRepository: StudyGroupMemberRepository
) {

    operator fun invoke(groupId: UUID, enrolStudyGroupMemberRequest: EnrolStudyGroupMemberRequest) {
        studyGroupMemberRepository.add(groupId, enrolStudyGroupMemberRequest)
    }
}