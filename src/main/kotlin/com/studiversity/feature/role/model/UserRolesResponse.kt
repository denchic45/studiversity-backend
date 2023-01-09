package com.studiversity.feature.role.model

import com.studiversity.feature.role.Role
import com.studiversity.ktor.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class UserRolesResponse(
    @Serializable(UUIDSerializer::class) val userId: UUID,
    val roles: List<Role>
)
