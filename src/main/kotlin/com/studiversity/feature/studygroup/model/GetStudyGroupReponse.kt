package com.studiversity.feature.studygroup.model

import kotlinx.serialization.Serializable

@Serializable
data class StudyGroupResponse(
    val id: String,
    val name: String,
    val academicYear: AcademicYear,
    val specialty: SpecialtyResponse?
)

@Serializable
data class SpecialtyResponse(val id: String, val name: String)