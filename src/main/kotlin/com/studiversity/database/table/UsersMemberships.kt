package com.studiversity.database.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp

object UsersMemberships : UUIDTable("user_membership", "user_membership_id") {
    val membershipId = reference("membership_id", Memberships.id)
    val userId = reference("user_id", Users.id)
    val joinTimestamp = timestamp("join_timestamp")
    val leaveTimestamp = timestamp("join_timestamp")
}