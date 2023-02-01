package com.studiversity.feature.timetable

import com.studiversity.database.table.*
import com.stuiversity.api.timetable.model.EventDetails
import com.stuiversity.api.timetable.model.LessonDetails
import com.stuiversity.api.timetable.model.PeriodRequest
import com.stuiversity.api.timetable.model.TimetableResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.between
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*

class TimetableRepository {

    fun putPeriodsOfDay(studyGroupId: UUID, date: LocalDate, periods: List<PeriodRequest>) {
        removeByStudyGroupIdAndDate(studyGroupId, date)
        periods.forEach { period ->
            val periodDao = PeriodDao.new {
                this.date = date
                order = period.order
                roomId = period.roomId
                this.studyGroupId = studyGroupId
                type = period.type
            }
            val periodId = periodDao.id.value

            when (val details = period.details) {
                is LessonDetails -> LessonDao.new(periodId) {
                    courseId = details.courseId
                }

                is EventDetails -> EventDao.new(periodId) {
                    name = details.name
                    color = details.color
                    icon = details.icon
                }
            }

            period.memberIds.forEach {
                val value = PeriodMemberDao.new {
                    this.period = periodDao
                    this.member = UserDao.findById(it)!!
                }

                println("PeriodMember: $value")
            }
        }
    }

    fun findByRangeDates(
        startDate: LocalDate,
        endDate: LocalDate,
        studyGroupId: List<UUID>? = null,
        memberIds: List<UUID>? = null,
        courseIds: List<UUID>? = null,
        roomIds: List<UUID>? = null
    ): TimetableResponse {
        val query = Periods.select(Periods.date.between(startDate, endDate))

        studyGroupId?.let { query.andWhere { Periods.studyGroupId inList it } }

        courseIds?.let {
            query.adjustColumnSet { innerJoin(Lessons, { Periods.id }, { id }) }
                .adjustSlice { slice(fields + Lessons.columns) }
                .andWhere { Lessons.courseId inList courseIds }
        }

        memberIds?.let {
            query.adjustColumnSet { innerJoin(PeriodsMembers, { Periods.id }, { periodId }) }
                .adjustSlice { slice(fields + PeriodsMembers.columns) }
                .andWhere { PeriodsMembers.memberId inList memberIds }
        }

        roomIds?.let {
            query.andWhere { Periods.roomId inList roomIds }
        }

        return PeriodDao.wrapRows(query)
            .orderBy(Periods.date to SortOrder.ASC, Periods.order to SortOrder.ASC)
            .map(PeriodDao::toResponse)
            .groupBy { it.date.dayOfWeek }
            .withDefault { emptyList() }
            .let { map ->
                TimetableResponse(
                    monday = map.getValue(DayOfWeek.MONDAY),
                    tuesday = map.getValue(DayOfWeek.TUESDAY),
                    wednesday = map.getValue(DayOfWeek.WEDNESDAY),
                    thursday = map.getValue(DayOfWeek.THURSDAY),
                    friday = map.getValue(DayOfWeek.FRIDAY),
                    saturday = map.getValue(DayOfWeek.SATURDAY),
                )
            }
    }

    fun removeByStudyGroupIdAndDate(studyGroupId: UUID, date: LocalDate) {
        Periods.deleteWhere { Periods.studyGroupId eq studyGroupId and (Periods.date eq date) }
    }
}