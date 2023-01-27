package com.studiversity.feature.user

import com.stuiversity.api.auth.model.CreateUserRequest
import com.stuiversity.api.auth.model.SignupRequest
import com.stuiversity.api.user.model.Account
import com.stuiversity.api.user.model.User
import java.util.*

fun SignupRequest.toUser(id: UUID) = User(
    id = id,
    firstName = firstName,
    surname = surname,
    patronymic = patronymic,
    account = Account(email)
)

fun CreateUserRequest.toUser(id: UUID) = User(
    id = id,
    firstName = firstName,
    surname = surname,
    patronymic = patronymic,
    account = Account(email)
)