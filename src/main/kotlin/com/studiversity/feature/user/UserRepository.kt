package com.studiversity.feature.user

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.studiversity.database.table.AuthUsers
import com.studiversity.database.table.UserDao
import com.studiversity.database.table.Users
import com.studiversity.database.table.toDomain
import com.studiversity.feature.auth.PasswordGenerator
import com.studiversity.feature.role.ScopeType
import com.studiversity.feature.role.repository.AddScopeRepoExt
import com.studiversity.feature.user.account.model.PutUserSupabaseRequest
import com.studiversity.supabase.model.SignUpGoTrueResponse
import com.studiversity.supabase.model.asSupabaseErrorResponse
import com.studiversity.util.EmailSender
import com.stuiversity.api.account.model.UpdateAccountPersonalRequest
import com.stuiversity.api.account.model.UpdateEmailRequest
import com.stuiversity.api.account.model.UpdatePasswordRequest
import com.stuiversity.api.auth.model.*
import com.stuiversity.api.common.EmptyResponseResult
import com.stuiversity.api.common.ResponseResult
import com.stuiversity.api.user.model.User
import com.stuiversity.util.OptionalProperty
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import java.util.*

class UserRepository(
    private val organizationId: UUID,
    private val supabaseClient: HttpClient,
    private val emailSender: EmailSender
) : AddScopeRepoExt {

    fun add(user: User) {
        UserDao.new(user.id) {
            firstName = user.firstName
            surname = user.surname
            patronymic = user.patronymic
            email = user.account.email
        }
        addScope(user.id, ScopeType.User, organizationId)
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

    suspend fun add(createUserRequest: CreateUserRequest): ResponseResult<User> {
        val password = PasswordGenerator().generate()
        val response = signUpUser(createUserRequest.email, password)
        if (!response.status.isSuccess())
            return Err(response.asSupabaseErrorResponse())

        add(createUserRequest.toUser(response.body<SignUpGoTrueResponse>().user.id))

        emailSender.sendSimpleEmail(
            createUserRequest.email,
            "Регистрация",
            generateEmailMessage(createUserRequest.firstName, createUserRequest.email, password)
        )
        return Ok(findById(response.body<SignUpGoTrueResponse>().user.id)!!)
    }

    private fun generateEmailMessage(firstName: String, email: String, password: String): String {
        return """
            Здравствуйте, $firstName
            
            Вы были успешно зарегистрированы! Ваши данные для авторизации:
            email: $email
            пароль: $password
        """.trimIndent()
    }

    fun findById(id: UUID): User? {
        return UserDao.findById(id)?.toDomain()
    }

    fun remove(userId: UUID): Boolean {
        return (AuthUsers.deleteWhere { AuthUsers.id eq userId } == 1
                && Users.deleteWhere { Users.id eq userId } == 1)
    }

    fun update(userId: UUID, updateAccountPersonalRequest: UpdateAccountPersonalRequest) {
        UserDao.findById(userId)!!.apply {
            updateAccountPersonalRequest.firstName.ifPresent {
                firstName = it
            }
            updateAccountPersonalRequest.surname.ifPresent {
                surname = it
            }
            updateAccountPersonalRequest.patronymic.ifPresent {
                patronymic = it
            }
        }
    }

    suspend fun update(userId: UUID, updatePasswordRequest: UpdatePasswordRequest): EmptyResponseResult {
        val tokenResponse = signInByEmailAndPassword(userId, updatePasswordRequest.oldPassword)
        if (!tokenResponse.status.isSuccess())
            return Err(tokenResponse.asSupabaseErrorResponse())

        val updatePassword = supabaseClient.put("/auth/v1/user") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${tokenResponse.body<SignUpGoTrueResponse>().accessToken}")
            setBody(PutUserSupabaseRequest(password = OptionalProperty.of(updatePasswordRequest.newPassword)))
        }

        if (!updatePassword.status.isSuccess())
            return Err(updatePassword.asSupabaseErrorResponse())

        return Ok(Unit)
    }

    private suspend fun signInByEmailAndPassword(
        userId: UUID,
        password: String
    ) = supabaseClient.post("/auth/v1/token") {
        setBody(SignInByEmailPasswordRequest(UserDao.findById(userId)!!.email, password))
        contentType(ContentType.Application.Json)
        parameter("grant_type", "password")
    }

    suspend fun update(userId: UUID, updateEmailRequest: UpdateEmailRequest): EmptyResponseResult {
        val tokenResponse = signInByEmailAndPassword(userId, updateEmailRequest.password)
        if (!tokenResponse.status.isSuccess())
            return Err(tokenResponse.asSupabaseErrorResponse())

        val updateEmail = supabaseClient.put("/auth/v1/user") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${tokenResponse.body<SignUpGoTrueResponse>().accessToken}")
            setBody(PutUserSupabaseRequest(email = OptionalProperty.of(updateEmailRequest.email)))
        }

        if (!updateEmail.status.isSuccess())
            return Err(updateEmail.asSupabaseErrorResponse())

        return Ok(Unit)
    }
}