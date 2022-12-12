package com.studiversity.feature.user

import java.util.*

data class User(
    val id: UUID,
    val firstName: String,
    val surname: String,
    val patronymic: String?,
    val account: Account
)