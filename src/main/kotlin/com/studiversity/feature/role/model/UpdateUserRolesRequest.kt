package com.studiversity.feature.role.model

import com.studiversity.feature.role.Role
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRolesRequest(val roles: List<Role>)
