package com.studiversity.feature.membership.model

import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class CreateMembershipRequest(
    val type: String,
    @Serializable(UUIDSerializer::class)
    val scopeId: UUID,
    val details: MembershipDetails? = null
)

sealed interface MembershipDetails

@Serializable
data class StudyGroupExternalMembershipDetails(
    @Serializable(UUIDSerializer::class)
    val studyGroupId: UUID
) : MembershipDetails