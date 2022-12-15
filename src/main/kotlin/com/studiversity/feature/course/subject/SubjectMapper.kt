package com.studiversity.feature.course.subject

import com.studiversity.database.table.SubjectDao
import com.studiversity.feature.course.subject.model.SubjectResponse
import org.jetbrains.exposed.sql.SizedIterable

fun SubjectDao.toResponse() = SubjectResponse(id = id.value, name = name, iconName = iconName)

fun SizedIterable<SubjectDao>.toResponses() = map(SubjectDao::toResponse)