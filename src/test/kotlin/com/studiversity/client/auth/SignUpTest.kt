package com.studiversity.client.auth

import com.github.michaelbull.result.unwrap
import com.studiversity.KtorClientTest
import com.studiversity.util.assertResultErr
import com.studiversity.util.assertResultOk
import com.stuiversity.api.auth.AuthApi
import com.stuiversity.api.auth.model.SignupRequest
import com.stuiversity.api.user.UserApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlin.test.assertEquals

class SignUpTest : KtorClientTest() {


    private val email = "yar256@mail.ru"
    private val password = "KLNf94fghn4gg"
    private val expectedFirstName = "Yaroslav"
    private val expectedSurname = "Sokolov"

    private val userClient by lazy { createAuthenticatedClient(email, password) }
    private val guestClient by lazy { createGuestClient() }

    private val authApiOfGuest: AuthApi by inject { parametersOf(guestClient) }
    private val userApiOfUser: UserApi by inject { parametersOf(userClient) }
    private val userApiOfModerator: UserApi by inject { parametersOf(client) }

    @Test
    fun testSignUp(): Unit = runBlocking {
        val signupRequest = SignupRequest(expectedFirstName, expectedSurname, null, email, password)
        val token = authApiOfGuest.signUp(signupRequest)
            .also(::assertResultOk).unwrap()

        authApiOfGuest.signUp(signupRequest).also(::assertResultErr)

        val user = userApiOfUser.getMe().also(::assertResultOk).unwrap().apply {
            assertEquals(expectedFirstName, firstName)
            assertEquals(expectedSurname, surname)
        }
        userApiOfModerator.remove(user.id).also(::assertResultOk)
    }

}