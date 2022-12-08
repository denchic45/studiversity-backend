package com.studiversity.feature.group.mapper

import com.studiversity.database.table.SpecialtyDao
import com.studiversity.feature.group.model.Specialty

fun SpecialtyDao.toSpecialty() = Specialty(
    id = id.value,
    name = name
)