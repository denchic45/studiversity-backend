package com.studiversity.feature.group.mapper

import com.studiversity.database.table.SpecialtyDao
import com.studiversity.feature.group.dto.SpecialtyResponse
import com.studiversity.feature.group.model.Specialty

fun SpecialtyDao.toSpecialty() = Specialty(
    id = id.value,
    name = name
)

fun SpecialtyDao.toResponse() = SpecialtyResponse(
    id = id.value.toString(),
    name = name
)