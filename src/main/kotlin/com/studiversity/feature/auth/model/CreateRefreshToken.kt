package com.studiversity.feature.auth.model

import java.time.Instant
import java.util.*

data class CreateRefreshToken(
    val userId: UUID,
    val token: String,
    val expireAt: Instant
)
