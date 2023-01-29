package com.studiversity.client.user

import com.github.michaelbull.result.unwrap
import com.studiversity.KtorClientTest
import com.studiversity.util.assertResultOk
import com.stuiversity.api.auth.model.CreateUserRequest
import com.stuiversity.api.user.UserApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlin.test.assertEquals

class CreateUserTest : KtorClientTest() {

    private val userApi: UserApi by inject { parametersOf(client) }
    private val email = "denchic860@gmail.com"
    private val expectedFirstName = "Yaroslav"
    private val expectedSurname = "Sokolov"
    private val userApiOfModerator: UserApi by inject { parametersOf(client) }

    @Test
    fun test(): Unit = runBlocking {
        val user = userApi.create(CreateUserRequest(expectedFirstName, expectedSurname, null, email))
            .also(::assertResultOk).unwrap()

        userApiOfModerator.getById(user.id).also(::assertResultOk).unwrap().apply {
            assertEquals(expectedFirstName, firstName)
            assertEquals(expectedSurname, surname)
        }

        userApiOfModerator.remove(user.id).also(::assertResultOk)
    }
}