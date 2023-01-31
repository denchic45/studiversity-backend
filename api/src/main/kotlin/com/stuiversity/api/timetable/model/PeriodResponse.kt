package com.stuiversity.api.timetable.model

import com.stuiversity.util.LocalDateSerializer
import com.stuiversity.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.*

sealed class PeriodResponse {
    abstract val id: Long

    @Serializable(LocalDateSerializer::class)
    abstract val date: LocalDate
    abstract val order: Short

    @Serializable(UUIDSerializer::class)
    abstract val roomId: UUID

    @Serializable(UUIDSerializer::class)
    abstract val studyGroupId: UUID
    abstract val type: PeriodType
    abstract val details: PeriodDetails
}

enum class PeriodType { LESSON, EVENT }