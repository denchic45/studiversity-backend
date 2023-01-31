package com.stuiversity.api.timetable.model

import com.stuiversity.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
sealed interface PeriodDetails

@Serializable
data class Lesson(
    @Serializable(UUIDSerializer::class)
    val courseId: UUID
) : PeriodDetails

@Serializable
data class Event(
    val name: String,
    val color: String,
    val icon: String
) : PeriodDetails