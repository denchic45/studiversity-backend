package com.studiversity.feature.studygroup.model

import kotlinx.serialization.Serializable

@Serializable
data class EnrolStudyGroupMemberRequest(
    val userId: String,
    val roles: List<String>
)
