package com.studiversity.feature.role.model

import com.stuiversity.api.role.Role
import com.stuiversity.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UserRolesResponse(
    @Serializable(UUIDSerializer::class) val userId: UUID,
    val roles: List<Role>
)
