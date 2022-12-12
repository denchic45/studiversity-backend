package com.studiversity.feature.studygroup.domain

import com.studiversity.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class StudyGroupMembers(
    @Serializable(UUIDSerializer::class)
    val studyGroupId: UUID,
    val members: List<StudyGroupMember>
)

@Serializable
data class StudyGroupMember(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val firstName: String,
    val surname: String,
    val patronymic: String?,
    val roles: List<String>
)
