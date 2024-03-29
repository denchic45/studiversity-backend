package com.studiversity.database.table

import com.studiversity.database.type.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object MagicLinks : LongIdTable("magic_link") {
    val token = text("token")
    val userId = reference("user_id", Users)
    val expireAt = timestampWithTimeZone("expire_at")
}