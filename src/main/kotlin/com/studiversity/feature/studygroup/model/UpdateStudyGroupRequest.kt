package com.studiversity.feature.studygroup.model

import com.studiversity.util.OptionalProperty
import com.studiversity.util.OptionalPropertySerializer
import com.studiversity.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UpdateStudyGroupRequest(
    @Serializable(with = OptionalPropertySerializer::class)
    val name: OptionalProperty<String> = OptionalProperty.NotPresent,
    @Serializable(with = OptionalPropertySerializer::class)
    val academicYear: OptionalProperty<AcademicYear> = OptionalProperty.NotPresent,
    @Serializable(with = OptionalPropertySerializer::class)
    val specialtyId: OptionalProperty<String?> = OptionalProperty.NotPresent,
    @Serializable(with = OptionalPropertySerializer::class)
    val curatorId: OptionalProperty<@Serializable(UUIDSerializer::class) UUID?> = OptionalProperty.NotPresent
)