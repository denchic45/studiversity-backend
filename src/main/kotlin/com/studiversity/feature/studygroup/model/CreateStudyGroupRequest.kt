package com.studiversity.feature.studygroup.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateStudyGroupRequest(
    val name: String,
    val academicYear: AcademicYear,
    val specialtyId: String? = null,
    val curatorId: String? = null
)

@Serializable
data class AcademicYear(val start: Int, val end: Int)