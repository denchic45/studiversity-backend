package com.studiversity.feature.timetable.usecase

import com.studiversity.feature.timetable.TimetableRepository
import com.studiversity.transaction.TransactionWorker
import com.stuiversity.api.timetable.model.PutTimetableRequest
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*

class PutTimetableUseCase(
    private val transactionWorker: TransactionWorker,
    private val timetableRepository: TimetableRepository
) {
    operator fun invoke(studyGroupId: UUID, weekOfYear: String, putTimetableRequest: PutTimetableRequest) =
        transactionWorker {
            val monday = LocalDate.parse(
                weekOfYear, DateTimeFormatterBuilder()
                    .appendPattern("YYYY_ww")
                    .parseDefaulting(ChronoField.DAY_OF_WEEK, DayOfWeek.MONDAY.value.toLong())
                    .toFormatter()
            )
            timetableRepository.putPeriodsOfDay(studyGroupId, monday.plusDays(0), putTimetableRequest.monday)
            timetableRepository.putPeriodsOfDay(studyGroupId, monday.plusDays(1), putTimetableRequest.tuesday)
            timetableRepository.putPeriodsOfDay(studyGroupId, monday.plusDays(2), putTimetableRequest.wednesday)
            timetableRepository.putPeriodsOfDay(studyGroupId, monday.plusDays(3), putTimetableRequest.thursday)
            timetableRepository.putPeriodsOfDay(studyGroupId, monday.plusDays(4), putTimetableRequest.friday)
            timetableRepository.putPeriodsOfDay(studyGroupId, monday.plusDays(5), putTimetableRequest.saturday)

            timetableRepository.findByStudyGroupIdAndRangeDates(studyGroupId, monday, monday.plusDays(5))
        }
}