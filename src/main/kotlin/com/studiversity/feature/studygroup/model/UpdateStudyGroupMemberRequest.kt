package com.studiversity.feature.studygroup.model

import com.studiversity.feature.role.Role
import com.studiversity.util.OptionalProperty
import com.studiversity.util.OptionalPropertySerializer
import kotlinx.serialization.Serializable

@Serializable
data class UpdateStudyGroupMemberRequest(
    @Serializable(OptionalPropertySerializer::class)
    val roles: OptionalProperty<List<Role>> = OptionalProperty.NotPresent
)
