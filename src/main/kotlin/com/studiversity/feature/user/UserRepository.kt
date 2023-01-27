package com.studiversity.feature.user

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.studiversity.Constants
import com.studiversity.database.table.UserDao
import com.studiversity.database.table.toDomain
import com.studiversity.feature.auth.PasswordGenerator
import com.studiversity.feature.auth.model.CreateUserRequest
import com.studiversity.feature.auth.model.SignUpGoTrueRequest
import com.studiversity.feature.auth.model.SignupRequest
import com.studiversity.feature.auth.model.TokenResponse
import com.studiversity.feature.role.ScopeType
import com.studiversity.feature.role.repository.AddScopeRepoExt
import com.studiversity.supabase.model.SignupGoTrueResponse
import com.studiversity.supabase.model.asSupabaseErrorResponse
import com.stuiversity.api.util.ResponseResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class UserRepository(
    private val supabaseClient: HttpClient
) : AddScopeRepoExt {

    fun add(user: User) {
        transaction {
            UserDao.new(user.id) {
                firstName = user.firstName
                surname = user.firstName
                patronymic = user.patronymic
                email = user.account.email
            }
            addScope(user.id, ScopeType.User, Constants.organizationId)
        }
    }

    suspend fun add(signupRequest: SignupRequest): ResponseResult<TokenResponse> {
        val response = supabaseClient.post("/auth/v1/signup") {
            setBody(SignUpGoTrueRequest(signupRequest.email, signupRequest.password))
            contentType(ContentType.Application.Json)
        }

        if (!response.status.isSuccess())
            return Err(response.asSupabaseErrorResponse())

        val signupResponse = response.body<SignupGoTrueResponse>()
        add(
            User(
                signupResponse.userGoTrue.id,
                signupRequest.firstName,
                signupRequest.surname,
                signupRequest.patronymic,
                Account(signupRequest.email)
            )
        )

        return Ok(TokenResponse(signupResponse.accessToken, signupResponse.refreshToken))
    }

    suspend fun add(createUserRequest: CreateUserRequest) {
        val response = supabaseClient.post("/auth/v1/signup") {
            setBody(SignUpGoTrueRequest(createUserRequest.email, PasswordGenerator().generate()))
            contentType(ContentType.Application.Json)
        }
    }

    fun findById(id: UUID): User? {
        return UserDao.findById(id)?.toDomain()
    }
}