package com.studiversity.feature.membership.model

import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Member(
    @SerialName("user_id")
    @Serializable(UUIDSerializer::class)
    val userId: UUID,
    @SerialName("membership_id")
    @Serializable(UUIDSerializer::class)
    val membershipId: UUID
)
