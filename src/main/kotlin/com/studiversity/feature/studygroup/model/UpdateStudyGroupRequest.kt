package com.studiversity.feature.studygroup.model

import com.studiversity.util.OptionalProperty
import com.studiversity.util.OptionalPropertySerializer
import kotlinx.serialization.Serializable

@Serializable
data class UpdateStudyGroupRequest(
    @Serializable(with = OptionalPropertySerializer::class)
    val name: OptionalProperty<String> = OptionalProperty.NotPresent,
    @Serializable(with = OptionalPropertySerializer::class)
    val academicYear: OptionalProperty<AcademicYear> = OptionalProperty.NotPresent,
    @Serializable(with = OptionalPropertySerializer::class)
    val specialtyId: OptionalProperty<String?> = OptionalProperty.NotPresent,
    @Serializable(with = OptionalPropertySerializer::class)
    val curatorId: OptionalProperty<String?> = OptionalProperty.NotPresent
)