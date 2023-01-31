package com.stuiversity.api.timetable.model

import kotlinx.serialization.Serializable

@Serializable
data class PutTimetableRequest constructor(
    val monday: List<PeriodRequest>,
    val tuesday: List<PeriodRequest>,
    val wednesday: List<PeriodRequest>,
    val thursday: List<PeriodRequest>,
    val friday: List<PeriodRequest>,
    val saturday: List<PeriodRequest> = emptyList()
)