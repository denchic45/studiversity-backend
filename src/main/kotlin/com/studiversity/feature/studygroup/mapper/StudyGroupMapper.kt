package com.studiversity.feature.studygroup.mapper

import com.studiversity.database.table.StudyGroupDao
import com.studiversity.database.table.UserRoleScopeDao
import com.studiversity.feature.studygroup.domain.StudyGroupMember
import com.studiversity.feature.studygroup.domain.StudyGroupMembers
import com.studiversity.feature.studygroup.model.AcademicYear
import com.studiversity.feature.studygroup.model.StudyGroupResponse

//fun StudyGroupDao.toStudyGroup() = StudyGroup(
//    id = id.value,
//    name = name,
//    academicYear = StudyGroup.AcademicYear(Year.of(academicYear[0]), Year.of(academicYear[1])),
//    specialty = specialty?.toSpecialty()
//)

fun StudyGroupDao.toResponse() = StudyGroupResponse(
    id = id.value,
    name = name,
    academicYear = AcademicYear(academicYear[0], academicYear[1]),
    specialty = specialty?.toResponse()
)

fun Iterable<UserRoleScopeDao>.toStudyGroupMembers(): StudyGroupMembers = StudyGroupMembers(
    studyGroupId = first().scopeId,
    members = groupBy { it.user }
        .map { (user, userRoleScopeDaoList) ->
            user.let { userDao ->
                StudyGroupMember(id = userDao.id.value,
                    firstName = userDao.firstName,
                    surname = userDao.surname,
                    patronymic = userDao.patronymic,
                    roles = userRoleScopeDaoList.map { it.role.shortName }
                )
            }
        }
)