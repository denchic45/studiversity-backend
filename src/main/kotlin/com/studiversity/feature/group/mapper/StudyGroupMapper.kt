package com.studiversity.feature.group.mapper

import com.studiversity.database.table.StudyGroupDao
import com.studiversity.feature.group.model.StudyGroup
import java.time.Year

fun StudyGroupDao.toStudyGroup() = StudyGroup(
    id = id.value,
    name = name,
    academicYear = StudyGroup.AcademicYear(Year.of(academicYear[0]), Year.of(academicYear[1])),
    specialty = specialty?.toSpecialty()
)