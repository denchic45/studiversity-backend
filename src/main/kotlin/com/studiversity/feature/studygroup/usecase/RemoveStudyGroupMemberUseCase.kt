package com.studiversity.feature.studygroup.usecase

import com.studiversity.feature.studygroup.StudyGroupErrors
import com.studiversity.feature.studygroup.repository.StudyGroupMemberRepository
import io.ktor.server.plugins.*
import java.util.*

class RemoveStudyGroupMemberUseCase(private val studyGroupMemberRepository: StudyGroupMemberRepository) {

    operator fun invoke(groupId: UUID, memberId: UUID) {
        if (!studyGroupMemberRepository.isExist(groupId, memberId)) {
            throw BadRequestException(StudyGroupErrors.MEMBER_NOT_ENROLLED_IN_GROUP)
        }
        studyGroupMemberRepository.remove(groupId, memberId).let {
            if (!it)
                throw BadRequestException(StudyGroupErrors.FAILED_REMOVE_STUDY_GROUP_MEMBER)
        }
    }
}