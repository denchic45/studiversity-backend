package com.studiversity.feature.membership.model

import com.studiversity.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class JoinMemberRequest(
    @Serializable(UUIDSerializer::class)
    val userId: UUID,
    @Serializable(UUIDSerializer::class)
    val membershipId: UUID? = null,
    val roleIds: List<Long>
)
