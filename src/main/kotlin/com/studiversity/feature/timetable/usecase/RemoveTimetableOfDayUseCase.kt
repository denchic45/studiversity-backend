package com.studiversity.feature.timetable.usecase

import com.studiversity.feature.timetable.TimetableRepository
import com.studiversity.transaction.TransactionWorker
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import java.util.*

class RemoveTimetableOfDayUseCase(
    private val transactionWorker: TransactionWorker,
    private val timetableRepository: TimetableRepository
) {
    operator fun invoke(
        studyGroupId: UUID,
        weekOfYear: String,
        dayOfWeek: Int
    ) = transactionWorker {
        timetableRepository.removeByStudyGroupIdAndDate(
            studyGroupId = studyGroupId,
            date = LocalDate.parse(
                "${weekOfYear}_$dayOfWeek", DateTimeFormatterBuilder()
                    .appendPattern("YYYY_ww_e")
                    .toFormatter()
            ),
        )
    }
}