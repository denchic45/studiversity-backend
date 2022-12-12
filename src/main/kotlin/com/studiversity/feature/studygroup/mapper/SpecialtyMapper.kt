package com.studiversity.feature.studygroup.mapper

import com.studiversity.database.table.SpecialtyDao
import com.studiversity.feature.studygroup.model.SpecialtyResponse

//fun SpecialtyDao.toSpecialty() = Specialty(
//    id = id.value,
//    name = name
//)

fun SpecialtyDao.toResponse() = SpecialtyResponse(
    id = id.value,
    name = name
)