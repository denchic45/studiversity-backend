package com.studiversity.feature.timetable

import com.studiversity.database.table.EventDao
import com.studiversity.database.table.LessonDao
import com.studiversity.database.table.PeriodDao
import com.studiversity.database.table.Periods
import com.stuiversity.api.timetable.model.EventDetails
import com.stuiversity.api.timetable.model.LessonDetails
import com.stuiversity.api.timetable.model.PeriodRequest
import com.stuiversity.api.timetable.model.TimetableResponse
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.between
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*

class TimetableRepository {

    fun putPeriodsOfDay(studyGroupId: UUID, date: LocalDate, periods: List<PeriodRequest>) {
        removeByStudyGroupIdAndDate(studyGroupId, date)
        periods.forEach { period ->
            val periodId = PeriodDao.new {
                this.date = date
                order = period.order
                roomId = period.roomId
                this.studyGroupId = studyGroupId
                type = period.type
            }.id.value
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
        }
    }

    fun findByStudyGroupIdAndRangeDates(
        studyGroupId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): TimetableResponse {
        return PeriodDao.find(Periods.studyGroupId eq studyGroupId and (Periods.date.between(startDate, endDate)))
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