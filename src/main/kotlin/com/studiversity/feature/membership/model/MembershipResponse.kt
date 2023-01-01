package com.studiversity.feature.membership.model

import com.studiversity.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class MembershipResponse(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    val type: String
)
