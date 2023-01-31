package com.studiversity.database.table

import com.stuiversity.api.timetable.model.PeriodType
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.date

object Periods : LongIdTable("period", "period_id") {
    val date = date("date")
    val order = short("period_order")
    val roomId = uuid("room_id").references(
        Rooms.id,
        onDelete = ReferenceOption.SET_NULL,
        onUpdate = ReferenceOption.SET_NULL
    ).nullable()
    val studyGroupId = uuid("study_group_id").references(
        StudyGroups.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val type = enumeration<PeriodType>("period_type")
}