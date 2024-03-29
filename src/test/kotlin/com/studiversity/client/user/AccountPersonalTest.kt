package com.studiversity.client.user

import com.studiversity.KtorClientTest
import com.studiversity.util.assertedResultIsOk
import com.studiversity.util.unwrapAsserted
import com.stuiversity.api.account.AccountApi
import com.stuiversity.api.account.model.UpdateAccountPersonalRequest
import com.stuiversity.api.auth.model.SignupRequest
import com.stuiversity.api.user.UserApi
import com.stuiversity.api.user.model.User
import com.stuiversity.util.OptionalProperty
import io.ktor.client.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.parameter.parametersOf
import org.koin.test.inject
import kotlin.test.assertEquals

class AccountPersonalTest : KtorClientTest() {

    private val userApi: UserApi by inject { parametersOf(client) }
    private val accountApi: AccountApi by inject { parametersOf(userClient) }
    private val userApiOfUser: UserApi by inject { parametersOf(userClient) }

    private val email = "petya@gmail.com"
    private val password = "h9gf90G90v854"

    private lateinit var user: User
    private lateinit var userClient: HttpClient

    @BeforeEach
    fun init(): Unit = runBlocking {
        authApiOfGuest.signup(SignupRequest("Nikita", "Volkov", null, email, password))
            .assertedResultIsOk()
        userClient = createAuthenticatedClient(email, password)
        user = userApiOfUser.getMe().unwrapAsserted()
    }

    @AfterEach
    fun tearDown(): Unit = runBlocking {
        userApi.delete(user.id).assertedResultIsOk()
    }

    @Test
    fun testUpdatePersonal(): Unit = runBlocking {
        val expectedFirstName = "Nikolay"
        val expectedSurname = "Zakharov"
        val expectedPatronymic = "Pavlovich"
        accountApi.updatePersonal(
            UpdateAccountPersonalRequest(
                firstName = OptionalProperty.of(expectedFirstName),
                surname = OptionalProperty.of(expectedSurname),
                patronymic = OptionalProperty.of(expectedPatronymic)
            )
        ).unwrapAsserted()

        userApiOfUser.getMe().unwrapAsserted().apply {
            assertEquals(expectedFirstName, firstName)
            assertEquals(expectedSurname, surname)
            assertEquals(expectedPatronymic, patronymic)
        }
    }
}