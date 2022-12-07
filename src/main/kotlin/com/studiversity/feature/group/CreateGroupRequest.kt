package com.studiversity.feature.group

import kotlinx.serialization.Serializable

@Serializable
data class CreateGroupRequest(
    val name: String,
    val academicYear: AcademicYear,
    val curatorId: String
)

@Serializable
data class AcademicYear(val start: String, val end: String)