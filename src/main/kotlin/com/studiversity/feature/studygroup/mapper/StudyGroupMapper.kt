package com.studiversity.feature.studygroup.mapper

import com.studiversity.database.table.StudyGroupDao
import com.studiversity.feature.studygroup.model.AcademicYear
import com.studiversity.feature.studygroup.model.StudyGroupResponse

//fun StudyGroupDao.toStudyGroup() = StudyGroup(
//    id = id.value,
//    name = name,
//    academicYear = StudyGroup.AcademicYear(Year.of(academicYear[0]), Year.of(academicYear[1])),
//    specialty = specialty?.toSpecialty()
//)

fun StudyGroupDao.toResponse() = StudyGroupResponse(
    id = id.value.toString(),
    name = name,
    academicYear = AcademicYear(academicYear[0], academicYear[1]),
    specialty = specialty?.toResponse()
)