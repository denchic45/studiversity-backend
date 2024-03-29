package com.studiversity.database.table

import com.studiversity.util.varcharMax
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

object Rooms : UUIDTable("room", "room_id") {
    val name = varcharMax("room_name")
}

class RoomDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<RoomDao>(Rooms)

    var name by Rooms.name
}