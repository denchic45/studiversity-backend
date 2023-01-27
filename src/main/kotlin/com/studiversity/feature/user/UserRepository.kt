package com.studiversity.feature.user

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.studiversity.Constants
import com.studiversity.database.table.AuthUsers
import com.studiversity.database.table.UserDao
import com.studiversity.database.table.Users
import com.studiversity.database.table.toDomain
import com.studiversity.feature.auth.PasswordGenerator
import com.studiversity.feature.role.ScopeType
import com.studiversity.feature.role.repository.AddScopeRepoExt
import com.studiversity.supabase.model.SignUpGoTrueResponse
import com.studiversity.supabase.model.asSupabaseErrorResponse
import com.stuiversity.api.auth.model.CreateUserRequest
import com.stuiversity.api.auth.model.SignUpGoTrueRequest
import com.stuiversity.api.auth.model.SignupRequest
import com.stuiversity.api.auth.model.TokenResponse
import com.stuiversity.api.user.model.User
import com.stuiversity.api.util.EmptyResponseResult
import com.stuiversity.api.util.ResponseResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import java.util.*

class UserRepository(
    private val supabaseClient: HttpClient
) : AddScopeRepoExt {

    fun add(user: User) {
        UserDao.new(user.id) {
            firstName = user.firstName
            surname = user.surname
            patronymic = user.patronymic
            email = user.account.email
        }
        addScope(user.id, ScopeType.User, Constants.organizationId)
    }

    suspend fun add(signupRequest: SignupRequest): ResponseResult<TokenResponse> {
        val response = signUpUser(signupRequest.email, signupRequest.password)

        if (!response.status.isSuccess())
            return Err(response.asSupabaseErrorResponse())

        val signupResponse = response.body<SignUpGoTrueResponse>()
        add(signupRequest.toUser(signupResponse.user.id))

        return Ok(TokenResponse(signupResponse.accessToken, signupResponse.refreshToken))
    }

    private suspend fun signUpUser(email: String, password: String) = supabaseClient.post("/auth/v1/signup") {
        setBody(SignUpGoTrueRequest(email, password))
        contentType(ContentType.Application.Json)
    }

    suspend fun add(createUserRequest: CreateUserRequest): EmptyResponseResult {
        val response = signUpUser(createUserRequest.email, PasswordGenerator().generate())
        if (!response.status.isSuccess())
            return Err(response.asSupabaseErrorResponse())

        add(createUserRequest.toUser(response.body<SignUpGoTrueResponse>().user.id))

        return Ok(Unit)
    }

    fun findById(id: UUID): User? {
        return UserDao.findById(id)?.toDomain()
    }

    fun remove(userId: UUID): Boolean {
        return (AuthUsers.deleteWhere { AuthUsers.id eq userId } == 1
                && Users.deleteWhere { Users.id eq userId } == 1)
    }
}