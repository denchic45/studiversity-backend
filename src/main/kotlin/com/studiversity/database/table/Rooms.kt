package com.studiversity.database.table

import com.studiversity.util.varcharMax
import org.jetbrains.exposed.dao.id.UUIDTable

object Rooms : UUIDTable("room", "room_id") {
    val name = varcharMax("room_name")
}