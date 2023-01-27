package com.stuiversity.api.auth

import com.stuiversity.api.auth.model.SignInByEmailPasswordRequest
import com.stuiversity.api.auth.model.SignupRequest
import com.stuiversity.api.auth.model.TokenResponse
import com.stuiversity.api.util.ResponseResult
import com.stuiversity.api.util.toResult
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

interface AuthApi {
    suspend fun signUp(signupRequest: SignupRequest): ResponseResult<TokenResponse>

    suspend fun signInByEmailPassword(signInByEmailPasswordRequest: SignInByEmailPasswordRequest): ResponseResult<TokenResponse>
}

class AuthApiImpl(private val client: HttpClient) : AuthApi {
    override suspend fun signUp(signupRequest: SignupRequest): ResponseResult<TokenResponse> {
        return client.post("/auth/signUp") {
            contentType(ContentType.Application.Json)
            setBody(signupRequest)
        }.toResult()
    }

    override suspend fun signInByEmailPassword(signInByEmailPasswordRequest: SignInByEmailPasswordRequest):ResponseResult<TokenResponse> {
        return client.post("/auth/token") {
            contentType(ContentType.Application.Json)
            setBody(signInByEmailPasswordRequest)
            parameter("grant_type","password")
        }.toResult()
    }
}