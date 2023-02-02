package com.studiversity.feature.room.model

import com.stuiversity.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class RoomResponse(
    @Serializable(UUIDSerializer::class)
    val id: UUID,
    val name: String
)
