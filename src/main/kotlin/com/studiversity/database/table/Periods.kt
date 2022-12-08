package com.studiversity.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.date

object Periods : UUIDTable("period_id") {
    val date = date("date")
    val order = short("order")
    val roomId = uuid("room_id")
    val studyGroupId = uuid("study_group_id")
    val periodType = enumeration<PeriodType>("period_type")
}

enum class PeriodType { Lesson, Event }

fun main() {
    PeriodType.Lesson
}