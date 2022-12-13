package com.studiversity.feature.studygroup.mapper

import com.studiversity.database.table.StudyGroupDao
import com.studiversity.database.table.UserRoleScopeDao
import com.studiversity.feature.role.mapper.toRole
import com.studiversity.feature.studygroup.domain.StudyGroupMember
import com.studiversity.feature.studygroup.domain.StudyGroupMembers
import com.studiversity.feature.studygroup.model.AcademicYear
import com.studiversity.feature.studygroup.model.StudyGroupResponse
import java.util.UUID

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

fun Iterable<UserRoleScopeDao>.toStudyGroupMembers(groupId: UUID): StudyGroupMembers = StudyGroupMembers(
    studyGroupId = groupId,
    members = groupBy(UserRoleScopeDao::userId)
        .map { (userId, userRoleScopeDaoList) ->
            userRoleScopeDaoList.toStudyGroupMember()
        }
)

fun Iterable<UserRoleScopeDao>.toStudyGroupMember(): StudyGroupMember = first().user
    .let { userDao ->
        StudyGroupMember(id = userDao.id.value,
            firstName = userDao.firstName,
            surname = userDao.surname,
            patronymic = userDao.patronymic,
            roles = map { it.role.toRole() }
        )
    }
