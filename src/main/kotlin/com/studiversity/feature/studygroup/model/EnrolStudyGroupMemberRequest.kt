package com.studiversity.feature.studygroup.model

import com.stuiversity.api.role.Role
import com.stuiversity.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class EnrolStudyGroupMemberRequest(
    @Serializable(UUIDSerializer::class)
    val userId: UUID,
    val roles: List<Role>
)
