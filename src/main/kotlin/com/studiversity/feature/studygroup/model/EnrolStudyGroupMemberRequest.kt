package com.studiversity.feature.studygroup.model

import com.studiversity.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class EnrolStudyGroupMemberRequest(
    @Serializable(UUIDSerializer::class)
    val userId: UUID,
    val roles: List<String>
)
