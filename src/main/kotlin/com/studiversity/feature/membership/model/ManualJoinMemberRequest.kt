package com.studiversity.feature.membership.model

import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class ManualJoinMemberRequest(
    @Serializable(UUIDSerializer::class)
    val userId: UUID,
    val roleIds: List<Long>
)
