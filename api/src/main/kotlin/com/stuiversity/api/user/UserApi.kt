package com.stuiversity.api.user

import com.stuiversity.api.user.model.User
import com.stuiversity.api.util.EmptyResponseResult
import com.stuiversity.api.util.ResponseResult
import com.stuiversity.api.util.toResult
import io.ktor.client.*
import io.ktor.client.request.*
import java.util.*

interface UserApi {
    suspend fun getMe(): ResponseResult<User>

    suspend fun remove(userId: UUID): EmptyResponseResult
}

class UserApiImpl(private val client: HttpClient) : UserApi {
    override suspend fun getMe(): ResponseResult<User> {
        return client.get("/users/me").toResult()
    }

    override suspend fun remove(userId: UUID): EmptyResponseResult {
        return client.delete("/users/$userId").toResult()
    }
}