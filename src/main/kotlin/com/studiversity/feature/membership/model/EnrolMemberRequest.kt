package com.studiversity.feature.membership.model

import com.studiversity.feature.role.Role
import com.studiversity.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class EnrolMemberRequest(
    @Serializable(UUIDSerializer::class)
    val userId: UUID,
    val roles: List<Role>
)