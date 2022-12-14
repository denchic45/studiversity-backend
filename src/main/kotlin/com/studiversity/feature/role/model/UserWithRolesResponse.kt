package com.studiversity.feature.role.model

import com.studiversity.feature.role.Role
import com.studiversity.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UserWithRolesResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val firstName: String,
    val surname: String,
    val patronymic: String?,
    val roles: List<Role>
)
