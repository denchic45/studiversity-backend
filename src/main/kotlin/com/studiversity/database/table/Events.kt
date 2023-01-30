package com.studiversity.database.table

import com.studiversity.util.varcharMax
import org.jetbrains.exposed.dao.id.LongIdTable

object Events : LongIdTable("event", "period_id") {
    val name = varcharMax("event_name")
    val color = varcharMax("color")
    val icon = varcharMax("icon")
}