package com.studiversity.feature.studygroup.model

import com.studiversity.feature.role.Role
import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class EnrolStudyGroupMemberRequest(
    @Serializable(UUIDSerializer::class)
    val userId: UUID,
    val roles: List<Role>
)
