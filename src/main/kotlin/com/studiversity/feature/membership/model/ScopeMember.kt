package com.studiversity.feature.membership.model

import com.studiversity.feature.role.Role
import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class ScopeMember(
    @Serializable(UUIDSerializer::class)
    val userId: UUID,
    val firstName: String,
    val surname: String,
    val patronymic: String?,
    @Serializable(UUIDSerializer::class)
    val scopeId: UUID,
    val membershipIds: List<@Serializable(UUIDSerializer::class) UUID>,
    val roles: List<Role>
)